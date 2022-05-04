package org.playground.models;

import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.playground.pipe.dispatcher.PipeDispatcher;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WindowManagersTest {

    @InjectMocks
    private WindowManagers sut;
    @Mock
    private Map<String, WindowManager> windowManagers;
    @Mock
    private PipeDispatcher dispatcher;
    @Captor
    private ArgumentCaptor<WindowManager> windowManagerCaptor;
    private EasyRandom easyRandom;

    @Before
    public void setUp() {
        easyRandom = new EasyRandom();
    }

    @Test
    public void getInstance_WhenCalled_ReturnsAWindowManagers() {
        // when
        WindowManagers instance = WindowManagers.getInstance();
        // then
        assertThat(instance).isNotNull()
                .isInstanceOf(WindowManagers.class)
                .isNotNull();
    }

    @Test
    public void getInstance_WhenCalledMoreThanOnce_TheSameInstanceIsReturned() {
        // when
        WindowManagers instance1 = WindowManagers.getInstance();
        WindowManagers instance2 = WindowManagers.getInstance();
        // then
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    public void register_WhenCalled_TheWindowManagerIsTouchedAndPutIntoMap() {
        //given
        WindowManager windowManager = easyRandom.nextObject(WindowManager.class);
        when(dispatcher.touch(windowManager)).thenReturn(true);
        // when
        boolean actual = sut.register(windowManager);
        // then
        assertThat(actual).isTrue();
        verify(windowManagers).put(windowManager.getId(), windowManager);
        verify(dispatcher).touch(windowManager);
    }

    @Test
    public void register_DispatcherTouchReturnsFalse_FalseIsReturned() {
        //given
        WindowManager windowManager = easyRandom.nextObject(WindowManager.class);
        when(dispatcher.touch(windowManager)).thenReturn(false);
        // when
        boolean actual = sut.register(windowManager);
        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void unregister_WhenCalled_TheWindowManagerIsDeTouchedAndRemovedFromMap() {
        //given
        WindowManager windowManager = easyRandom.nextObject(WindowManager.class);
        when(dispatcher.deTouch(windowManager)).thenReturn(true);
        // when
        boolean actual = sut.unregister(windowManager);
        // then
        assertThat(actual).isTrue();
        verify(windowManagers).remove(windowManager.getId());
        verify(dispatcher).deTouch(windowManager);
    }

    @Test
    public void unregister_DispatcherDeTouchReturnsFalse_FalseIsReturned() {
        //given
        WindowManager windowManager = easyRandom.nextObject(WindowManager.class);
        when(dispatcher.deTouch(windowManager)).thenReturn(false);
        // when
        boolean actual = sut.unregister(windowManager);
        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void touchAll_WhenCalled_AllWindowManagersAreTouched() {
        // given
        List<WindowManager> windowManagerValues = easyRandom.objects(WindowManager. class, 3).collect(Collectors.toList());
        when(windowManagers.values()).thenReturn(windowManagerValues);
        when(dispatcher.touch(any(WindowManager.class))).thenReturn(true);
        // when
        boolean actual = sut.touchAll();
        // then
        assertThat(actual).isTrue();
        verify(dispatcher, times(3)).touch(windowManagerCaptor.capture());
        assertThat(windowManagerCaptor.getAllValues()).isNotNull().hasSize(3).containsExactly(windowManagerValues.toArray(new WindowManager[0]));
        verify(windowManagers).values();
    }

    @Test
    public void touchAll_DispatcherTouchReturnsOneFalse_FalseIsReturned() {
        // given
        List<WindowManager> windowManagerValues = easyRandom.objects(WindowManager. class, 3).collect(Collectors.toList());
        when(windowManagers.values()).thenReturn(windowManagerValues);
        when(dispatcher.touch(any(WindowManager.class))).thenReturn(true, false, true);
        // when
        boolean actual = sut.touchAll();
        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void unregisterAll_WhenCalled_AllWindowManagersAreDeTouchedAndMapIsEmptied() {
        // given
        List<WindowManager> windowManagerValues = easyRandom.objects(WindowManager. class, 3).collect(Collectors.toList());
        when(windowManagers.values()).thenReturn(windowManagerValues);
        when(dispatcher.deTouch(any(WindowManager.class))).thenReturn(true);
        // when
        boolean actual = sut.unregisterAll();
        // then
        assertThat(actual).isTrue();
        verify(dispatcher, times(3)).deTouch(windowManagerCaptor.capture());
        assertThat(windowManagerCaptor.getAllValues()).isNotNull().hasSize(3).containsExactly(windowManagerValues.toArray(new WindowManager[0]));
        verify(windowManagers).values();
        verify(windowManagers).clear();
    }

    @Test
    public void unregisterAll_DispatcherDeTouchReturnsOneFalse_FalseIsReturned() {
        // given
        List<WindowManager> windowManagerValues = easyRandom.objects(WindowManager. class, 3).collect(Collectors.toList());
        when(windowManagers.values()).thenReturn(windowManagerValues);
        when(dispatcher.deTouch(any(WindowManager.class))).thenReturn(true, false, true);
        // when
        boolean actual = sut.unregisterAll();
        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void getUsersWindowManager_WindowManagersMapIsEmpty_EmptySetIsReturned() {
        // given
        when(windowManagers.values()).thenReturn(Collections.emptyList());
        // when
        Set<WindowManager> actual = sut.getUsersWindowManager(easyRandom.nextLong());
        // then
        assertThat(actual).isNotNull().isEmpty();
    }

    @Test
    public void getUsersWindowManager_WindowManagersMapDoesNotContainProvidedUserId_EmptySetIsReturned() {
        // given
        Map<String, WindowManager> _windowManagers = easyRandom.objects(WindowManager.class, 3).collect(Collectors.toMap(WindowManager::getId, o -> o));
        sut = new WindowManagers(_windowManagers, dispatcher);
        Long userId = easyRandom.nextObject(Long.class);
        // when
        Set<WindowManager> actual = sut.getUsersWindowManager(userId);
        // then
        assertThat(actual).isNotNull().isEmpty();
    }

    @Test
    public void getUsersWindowManager_WindowManagersMapContainsProvidedUserId_WindowManagersOfUserAreReturned() {
        // given
        List<User> users = easyRandom.objects(User.class, 3).collect(Collectors.toList());
        List<WindowManager> expected = Arrays.asList(
                new WindowManager(users.get(2)),
                new WindowManager(users.get(2)));
        List<WindowManager> windowManagerList = Arrays.asList(
                new WindowManager(users.get(0)),
                new WindowManager(users.get(1)),
                expected.get(0),
                expected.get(1));
        Map<String, WindowManager> _windowManagers = windowManagerList.stream()
                        .collect(Collectors.toMap(WindowManager::getId, o -> o));
        sut = new WindowManagers(_windowManagers, dispatcher);
        // when
        Set<WindowManager> actual = sut.getUsersWindowManager(users.get(2).getId());
        // then
        assertThat(actual).isNotNull().hasSize(2).containsExactlyInAnyOrder(expected.toArray(new WindowManager[0]));
    }
}