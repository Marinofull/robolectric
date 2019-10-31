package org.robolectric.shadows;


import android.content.Context;
import android.text.format.DateFormat;
import java.util.Locale;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(DateFormat.class)
public class ShadowDateFormat {

  @Implementation
  protected static java.text.DateFormat getTimeFormat(Context context) {
    return new java.text.SimpleDateFormat("HH:mm:ss", Locale.ROOT);
  }
}
