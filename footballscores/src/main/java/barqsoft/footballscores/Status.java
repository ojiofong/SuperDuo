package barqsoft.footballscores;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by oofong25 on 9/27/15.
 */
public class Status {

    public static final int STATUS_OK = 0;
    public static final int STATUS_SERVER_DOWN = 1;
    public static final int STATUS_SERVER_INVALID = 2;
    public static final int STATUS_UNKNOWN = 3;
    public static final int STATUS_NO_DATA = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_OK, STATUS_SERVER_DOWN, STATUS_SERVER_INVALID, STATUS_UNKNOWN, STATUS_NO_DATA})
    public @interface RequestStatus {
    }

}
