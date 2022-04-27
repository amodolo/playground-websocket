package org.playground.pipe.dispatcher;

import org.playground.models.WindowManager;

public interface Register {

    boolean touch(WindowManager wm);

    boolean deTouch(WindowManager wm);
}
