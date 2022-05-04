package org.playground.pipe.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.playground.pipe.model.Message;
import org.playground.pipe.model.TextMessage;

import javax.websocket.DecodeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.playground.pipe.utils.MessageDecoder.DESERIALIZATION_ERROR;

@RunWith(MockitoJUnitRunner.class)
public class MessageDecoderTest {

    @InjectMocks
    private MessageDecoder sut;
    @Mock
    private ObjectMapper mapper;
    private EasyRandom easyRandom;

    @Before
    public void setUp() {
        easyRandom = new EasyRandom();
    }

    @Test
    public void decode_ObjectMapperThrowsAJsonProcessingException_ADecodeExceptionIsThrown() throws JsonProcessingException {
        // given
        String messageToDecode = easyRandom.nextObject(String.class);
        JsonProcessingException exception = easyRandom.nextObject(JsonProcessingException.class);
        DecodeException expectedException = new DecodeException(messageToDecode, DESERIALIZATION_ERROR, exception);
        when(mapper.readValue(messageToDecode, Message.class)).thenThrow(exception);
        // when
        assertThatThrownBy(() -> sut.decode(messageToDecode))
                .isInstanceOf(DecodeException.class)
                .usingRecursiveComparison()
                .isEqualTo(expectedException);
    }

    @Test
    public void decode_ObjectMapperCalled_TheDecodedObjectIsReturned() throws JsonProcessingException, DecodeException {
        // given
        String messageToDecode = easyRandom.nextObject(String.class);
        Message<String> message = easyRandom.nextObject(TextMessage.class);
        when(mapper.readValue(messageToDecode, Message.class)).thenReturn(message);
        // when
        Message<?> actual = sut.decode(messageToDecode);
        // then
        assertThat(actual).isNotNull().isEqualTo(message);
    }

    @Test
    public void willDecode_NullInput_ReturnsFalse() {
        assertThat(sut.willDecode(null)).isFalse();
    }

    @Test
    public void willDecode_NotNullInput_ReturnsTrue() {
        assertThat(sut.willDecode(easyRandom.nextObject(String.class))).isTrue();
    }
}