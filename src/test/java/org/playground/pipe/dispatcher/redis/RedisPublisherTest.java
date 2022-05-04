package org.playground.pipe.dispatcher.redis;

import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.playground.pipe.model.DispatchError;
import org.playground.pipe.model.Message;
import org.playground.pipe.model.TextMessage;
import org.playground.pipe.utils.MessageEncoder;
import org.playground.pipe.utils.Pipe;
import org.playground.services.RedisService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import javax.websocket.EncodeException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.playground.pipe.dispatcher.redis.RedisConstants.CHANNEL_PREFIX;
import static org.playground.pipe.dispatcher.redis.RedisConstants.KEY_PREFIX;
import static org.playground.pipe.dispatcher.redis.RedisPublisher.EXPIRATION_SECONDS;

@RunWith(MockitoJUnitRunner.class)
public class RedisPublisherTest {

    @InjectMocks
    private RedisPublisher sut;
    @Mock
    private MessageEncoder messageEncoder;
    @Mock
    private Jedis client;
    @Captor
    private ArgumentCaptor<Pipe> pipeCaptor;
    @Mock
    private Transaction transaction;
    private RedisPublisher spiedSut;
    private EasyRandom easyRandom;

    @Before
    public void setUp() {
        easyRandom = new EasyRandom();
        // @Spy needs a no-args constructor...
        spiedSut = spy(sut);
    }

    @Test
    public void send_RedisTargetIdsEmpty_DispatchErrorIsReturned() {
        // given
        Message<String> message = easyRandom.nextObject(TextMessage.class);
        DispatchError expected = new DispatchError(DispatchError.ErrorCode.NO_TARGET_AVAILABLE);
        try (MockedStatic<RedisService> mockedRedisService = mockStatic(RedisService.class)) {
            mockedRedisService.when(RedisService::getClient).thenReturn(client);
            // when
            DispatchError actual = spiedSut.send(message);
            // then
            assertThat(actual).isNotNull().isEqualTo(expected);
            verify(spiedSut, never()).write(anySet(), ArgumentMatchers.any(), eq(client));
            verify(spiedSut, never()).write(any(Pipe.class), ArgumentMatchers.<Message<String>>any(), eq(client));
        }
    }

    @Test
    public void send_RedisTargetIdsNotEmptyAndTargetNameIsNull_WriteToAllTargetsIsCalled() {
        // given
        Message<String> message = new TextMessage(easyRandom.nextObject(String.class), easyRandom.nextObject(Pipe.class), new Pipe(easyRandom.nextLong(), null));
        Set<String> targets = easyRandom.objects(String.class, 3).collect(Collectors.toSet());
        when(client.smembers(KEY_PREFIX + message.getTarget().getUserId())).thenReturn(targets);
        doReturn(null).when(spiedSut).write(targets, message, client);
        try (MockedStatic<RedisService> mockedRedisService = mockStatic(RedisService.class)) {
            mockedRedisService.when(RedisService::getClient).thenReturn(client);
            // when
            DispatchError actual = spiedSut.send(message);
            // then
            assertThat(actual).isNull();
            verify(spiedSut).write(targets, message, client);
        }
    }

    @Test
    public void send_RedisTargetIdsNotEmptyAndTargetNameIsNotNull_WriteToTheTargetIsCalled() {
        // given
        Pipe target = easyRandom.nextObject(Pipe.class);
        Message<String> message = new TextMessage(easyRandom.nextObject(String.class), easyRandom.nextObject(Pipe.class), target);
        Set<String> targets = Set.of(target.getName());
        when(client.smembers(KEY_PREFIX + message.getTarget().getUserId())).thenReturn(targets);
        doReturn(null).when(spiedSut).write(target, message, client);
        try (MockedStatic<RedisService> mockedRedisService = mockStatic(RedisService.class)) {
            mockedRedisService.when(RedisService::getClient).thenReturn(client);
            // when
            DispatchError actual = spiedSut.send(message);
            // then
            assertThat(actual).isNull();
            verify(spiedSut).write(target, message, client);
        }
    }

    @Test
    public void send_RedisTargetIdsNotEmptyAndTargetIsUnknown_DispatchErrorIsReturned() {
        // given
        Pipe target = easyRandom.nextObject(Pipe.class);
        Message<String> message = new TextMessage(easyRandom.nextObject(String.class), easyRandom.nextObject(Pipe.class), target);
        Set<String> targets = easyRandom.objects(String.class, 3).collect(Collectors.toSet());
        when(client.smembers(KEY_PREFIX + message.getTarget().getUserId())).thenReturn(targets);
        DispatchError expected = new DispatchError(DispatchError.ErrorCode.UNKNOWN_TARGET);
        try (MockedStatic<RedisService> mockedRedisService = mockStatic(RedisService.class)) {
            mockedRedisService.when(RedisService::getClient).thenReturn(client);
            // when
            DispatchError actual = spiedSut.send(message);
            // then
            assertThat(actual).isNotNull().isEqualTo(expected);
            verify(spiedSut, never()).write(anySet(), ArgumentMatchers.any(), eq(client));
            verify(spiedSut, never()).write(any(Pipe.class), ArgumentMatchers.<Message<String>>any(), eq(client));
        }
    }

    @Test
    public void write_MoreThanOneTargetAndAtLeastOneCorrectlySent_NullIsReturned() {
        // given
        Message<String> message = easyRandom.nextObject(TextMessage.class);
        List<String> targets = easyRandom.objects(String.class, 3).collect(Collectors.toList());
        List<DispatchError> dispatchErrors = easyRandom.objects(DispatchError.class, 2).collect(Collectors.toList());
        doReturn(dispatchErrors.get(0), null, dispatchErrors.get(1)).when(spiedSut).write(any(Pipe.class), eq(message), eq(client));
        // when
        DispatchError actual = spiedSut.write(new HashSet<>(targets), message, client);
        // then
        assertThat(actual).isNull();
        verify(spiedSut, times(3)).write(pipeCaptor.capture(), eq(message), eq(client));
        assertThat(pipeCaptor.getAllValues()).isNotNull().hasSize(3).containsExactlyInAnyOrder(
                new Pipe(message.getTarget().getUserId(), targets.get(0)),
                new Pipe(message.getTarget().getUserId(), targets.get(1)),
                new Pipe(message.getTarget().getUserId(), targets.get(2))
        );
    }

    @Test
    public void write_MoreThanOneTargetAndNoMessageCorrectlySent_LastDispatchErrorOccurredIsReturned() {
        // given
        Message<String> message = easyRandom.nextObject(TextMessage.class);
        List<String> targets = easyRandom.objects(String.class, 3).collect(Collectors.toList());
        List<DispatchError> dispatchErrors = easyRandom.objects(DispatchError.class, 3).collect(Collectors.toList());
        doReturn(dispatchErrors.get(0), dispatchErrors.get(1), dispatchErrors.get(2)).when(spiedSut).write(any(Pipe.class), eq(message), eq(client));
        // when
        DispatchError actual = spiedSut.write(new HashSet<>(targets), message, client);
        // then
        assertThat(actual).isNotNull().isEqualTo(dispatchErrors.get(2));
    }

    @Test
    public void write_MessageEncoderThrowsEncodeException_DispatchErrorIsReturned() throws EncodeException {
        // given
        Message<String> message = easyRandom.nextObject(TextMessage.class);
        Pipe pipe = easyRandom.nextObject(Pipe.class);
        EncodeException encodeException = easyRandom.nextObject(EncodeException.class);
        DispatchError expected = new DispatchError(DispatchError.ErrorCode.INVALID_MESSAGE);
        when(client.multi()).thenReturn(transaction);
        when(messageEncoder.encode(message)).thenThrow(encodeException);
        // when
        DispatchError actual = sut.write(pipe, message, client);
        // then
        assertThat(actual).isNotNull().isEqualTo(expected);
    }

    @Test
    public void write_WriteIsRetried3Times_LastTimeEndsWithSuccessSoNullIsReturned() throws EncodeException {
        // given
        Message<String> message = easyRandom.nextObject(TextMessage.class);
        Pipe pipe = easyRandom.nextObject(Pipe.class);
        when(client.multi()).thenReturn(transaction);
        String encodedMessage = easyRandom.nextObject(String.class);
        when(messageEncoder.encode(message)).thenReturn(encodedMessage);
        when(transaction.exec()).thenReturn(null)
                .thenReturn(null)
                .thenReturn(easyRandom.objects(Object.class, 3).collect(Collectors.toList()));
        // when
        DispatchError actual = sut.write(pipe, message, client);
        // then
        assertThat(actual).isNull();
        verify(transaction, times(3)).lpush(KEY_PREFIX + pipe.getId(), encodedMessage);
        verify(transaction, times(3)).expire(KEY_PREFIX + pipe.getId(), EXPIRATION_SECONDS);
        verify(transaction, times(3)).publish(CHANNEL_PREFIX + pipe.getId(), pipe.getId());
        verify(transaction, times(3)).exec();
    }

    @Test
    public void write_WriteIsRetried3TimesWithoutSuccess_IllegalStateExceptionIsThrown() throws EncodeException {
        // given
        Message<String> message = easyRandom.nextObject(TextMessage.class);
        Pipe pipe = easyRandom.nextObject(Pipe.class);
        when(client.multi()).thenReturn(transaction);
        String encodedMessage = easyRandom.nextObject(String.class);
        when(messageEncoder.encode(message)).thenReturn(encodedMessage);
        when(transaction.exec()).thenReturn(null);
        // when
        assertThatThrownBy(() -> sut.write(pipe, message, client))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(String.format(RedisPublisher.IMPOSSIBLE_TO_SEND_MESSAGE_TO_REDIS, message, KEY_PREFIX + pipe.getId()));
        // then
        verify(transaction, times(3)).lpush(KEY_PREFIX + pipe.getId(), encodedMessage);
        verify(transaction, times(3)).expire(KEY_PREFIX + pipe.getId(), EXPIRATION_SECONDS);
        verify(transaction, times(3)).publish(CHANNEL_PREFIX + pipe.getId(), pipe.getId());
        verify(transaction, times(3)).exec();
    }
}