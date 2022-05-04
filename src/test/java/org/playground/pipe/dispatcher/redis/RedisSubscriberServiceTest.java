package org.playground.pipe.dispatcher.redis;

import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.playground.pipe.utils.Pipe;
import org.playground.services.RedisService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.playground.pipe.dispatcher.redis.RedisConstants.CHANNEL_PREFIX;

@RunWith(MockitoJUnitRunner.class)
public class RedisSubscriberServiceTest {

    @InjectMocks
    private RedisSubscriberService sut;
    @Mock
    private Map<String, RedisSubscriber> subscribers;
    @Mock
    private JedisPubSub pubSub;
    @Mock
    private Jedis client;
    @Mock
    private RedisSubscriber redisSubscriber;
    @Captor
    private ArgumentCaptor<String> channelCaptor;
    @Mock
    private RedisSubscriber subscriber;
    @Captor
    private ArgumentCaptor<Pipe> pipeCaptor;
    private EasyRandom easyRandom;

    @Before
    public void setUp() {
        easyRandom = new EasyRandom();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void run_UnknownHostExceptionIsThrown_NoSubscriptionHappens() {
        // given
        UnknownHostException expectedException = easyRandom.nextObject(UnknownHostException.class);
        when(client.isConnected()).thenReturn(true);
        try (MockedStatic<RedisService> mockedRedisService = mockStatic(RedisService.class);
             MockedStatic<InetAddress> mockedInetAddress = mockStatic(InetAddress.class)) {
            mockedRedisService.when(RedisService::getClient).thenReturn(client);
            mockedInetAddress.when(InetAddress::getLocalHost).thenThrow(expectedException);
            // when
            sut.run();
            // then
            verify(client).isConnected();
            verify(client, never()).subscribe(any(JedisPubSub.class), anyString());
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void run_UnknownHostExceptionIsNotThrown_TheExceptionIsLogged() {
        // given
        UUID uuid = easyRandom.nextObject(UUID.class);
        when(client.isConnected()).thenReturn(true);
        try (MockedStatic<RedisService> mockedRedisService = mockStatic(RedisService.class);
             MockedStatic<InetAddress> mockedInetAddress = mockStatic(InetAddress.class);
             MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            mockedRedisService.when(RedisService::getClient).thenReturn(client);
            mockedInetAddress.when(InetAddress::getLocalHost).thenReturn(easyRandom.nextObject(InetAddress.class));
            mockedUUID.when(UUID::randomUUID).thenReturn(uuid);
            // when
            sut.run();
            // then
            verify(client).isConnected();
            verify(client).subscribe(eq(pubSub), anyString());
        }
    }

    @Test
    public void subscribe_WhenCalled_AllDelegatedServicesAreCalled() {
        // given
        String channel = easyRandom.nextObject(String.class);
        // when
        sut.subscribe(redisSubscriber, channel);
        // then
        verify(pubSub).subscribe(channel);
        verify(subscribers).put(channel, redisSubscriber);
    }

    @Test
    public void unsubscribe_WhenCalledWithChannel_AllDelegatedServicesAreCalled() {
        // given
        String channel = easyRandom.nextObject(String.class);
        // when
        sut.unsubscribe(channel);
        // then
        verify(pubSub).unsubscribe(channel);
        verify(subscribers).remove(channel);
    }

    @Test
    public void unsubscribe_WhenCalledWithoutChannel_AllDelegatedServicesAreCalled() {
        // given
        Set<String> keys = easyRandom.objects(String.class, 3).collect(Collectors.toSet());
        when(subscribers.keySet()).thenReturn(keys);
        // when
        sut.unsubscribe();
        // then
        verify(pubSub, times(3)).unsubscribe(channelCaptor.capture());
        assertThat(channelCaptor.getAllValues()).isNotNull().hasSize(3).containsExactlyInAnyOrder(keys.toArray(String[]::new));
        verify(pubSub).unsubscribe();
        verify(subscribers).clear();
    }

    @Test
    public void onMessage_ChannelIsNotInteresting_NoSubscriberNotified() {
        // given
        String channel = easyRandom.nextObject(String.class);
        String message = easyRandom.nextObject(String.class);
        // when
        sut.onMessage(channel, message);
        // then
        verify(subscribers, never()).get(channel);
    }

    @Test
    public void onMessage_ChannelIsInteresting_TheAssociatedSubscriberIsNotified() {
        // given
        Long userId = easyRandom.nextObject(Long.class);
        String name = easyRandom.nextObject(String.class);
        String channel = CHANNEL_PREFIX + userId +"_" + name;
        String message = easyRandom.nextObject(String.class);
        when(subscribers.get(channel)).thenReturn(subscriber);
        // when
        sut.onMessage(channel, message);
        // then
        verify(subscribers).get(channel);
        verify(subscriber).onMessage(pipeCaptor.capture());
        assertThat(pipeCaptor.getValue()).isNotNull().isEqualTo(new Pipe(userId, name));
    }

    @Test
    public void onMessage_ChannelIsInterestingButTheSubscriberIsNotFound_NoSubscriberNotified() {
        // given
        Long userId = easyRandom.nextObject(Long.class);
        String name = easyRandom.nextObject(String.class);
        String channel = CHANNEL_PREFIX + userId +"_" + name;
        String message = easyRandom.nextObject(String.class);
        // when
        sut.onMessage(channel, message);
        // then
        verify(subscribers).get(channel);
        verify(subscriber, never()).onMessage(any(Pipe.class));
    }
}