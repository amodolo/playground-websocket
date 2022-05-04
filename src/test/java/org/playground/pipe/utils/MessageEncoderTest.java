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

import javax.websocket.EncodeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.playground.pipe.utils.MessageEncoder.SERIALIZATION_ERROR;

@RunWith(MockitoJUnitRunner.class)
public class MessageEncoderTest {

    @InjectMocks
    private MessageEncoder sut;
    @Mock
    private ObjectMapper mapper;
    private EasyRandom easyRandom;

    @Before
    public void setUp() {
        easyRandom = new EasyRandom();
    }

    @Test
    public void encode_ObjectMapperThrowsAJsonProcessingException_AnEncodeExceptionIsThrown() throws JsonProcessingException {
        // given
        Message<String> messageToEncode = easyRandom.nextObject(TextMessage.class);
        JsonProcessingException exception = easyRandom.nextObject(JsonProcessingException.class);
        EncodeException expectedException = new EncodeException(messageToEncode, SERIALIZATION_ERROR, exception);
        when(mapper.writeValueAsString(messageToEncode)).thenThrow(exception);
        // when
        assertThatThrownBy(() -> sut.encode(messageToEncode))
                .isInstanceOf(EncodeException.class)
                .usingRecursiveComparison()
                .isEqualTo(expectedException);
    }

    @Test
    public void encode_ObjectMapperCalled_TheEncodedObjectIsReturned() throws JsonProcessingException, EncodeException {
        // given
        String expected = easyRandom.nextObject(String.class);
        Message<String> messageToEncode = easyRandom.nextObject(TextMessage.class);
        when(mapper.writeValueAsString(messageToEncode)).thenReturn(expected);
        // when
        String actual = sut.encode(messageToEncode);
        // then
        assertThat(actual).isNotNull().isEqualTo(expected);
    }
}