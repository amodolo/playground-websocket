package org.playground.pipe.dispatcher;

import org.playground.models.WindowManager;

public interface Register {

    boolean register(WindowManager wm);

    boolean unregister(WindowManager wm);

    boolean touch(WindowManager wm);

    boolean detouch(WindowManager wm);
}
