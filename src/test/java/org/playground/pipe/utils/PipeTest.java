package org.playground.pipe.utils;

import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PipeTest {

    private EasyRandom easyRandom;

    @Before
    public void setUp() {
        easyRandom = new EasyRandom();
    }

    @SuppressWarnings("LambdaBodyCanBeCodeBlock")
    @Test
    public void construct_ValueThrowsNumberFormatException_NumberFormatExceptionIsThrown() {
        assertThatThrownBy(() -> new Pipe(easyRandom.nextObject(String.class)))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void construct_ValueIsCorrect_NewPipeInstance() {
        // given
        Long userId = easyRandom.nextObject(Long.class);
        String name = easyRandom.nextObject(String.class);
        Pipe expected = new Pipe(userId, name);
        // when
        Pipe actual = new Pipe(userId + "_" + name);
        // then
        assertThat(actual).isNotNull().isEqualTo(expected);
    }

    @Test
    public void getId_WhenCalled_ReturnsTheCalculatedId() {
        // given
        Long userId = easyRandom.nextObject(Long.class);
        String name = easyRandom.nextObject(String.class);
        String expected = userId + "_" + name;
        // when
        String actual = new Pipe(userId, name).getId();
        // then
        assertThat(actual).isNotNull().isEqualTo(expected);
    }
}