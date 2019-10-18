package itp341.caceres.nicholas.positive_note.app;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

// BEWARE build variant matters! Release version contains no tests, only debug!

@RunWith(AndroidJUnit4.class)
public class MainInstrumentTest {
  @Test
  public void useAppContext() {
    // Context of the app under test.
    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    assertEquals("edu.usc.nlcaceres.infectionprevention", appContext.getPackageName());
  }
}
