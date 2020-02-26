package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.os.Build.VERSION_CODES;
import android.telephony.ims.ImsException;
import android.telephony.ims.ImsMmTelManager.CapabilityCallback;
import android.telephony.ims.ImsMmTelManager.RegistrationCallback;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.feature.MmTelFeature.MmTelCapabilities;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowImsMmTelManager} */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.Q)
public class ShadowImsMmTelManagerTest {

  private ShadowImsMmTelManager shadowImsMmTelManager;

  @Before
  public void setup() {
    shadowImsMmTelManager = new ShadowImsMmTelManager();
  }

  @Test
  public void registerImsRegistrationCallback_imsRegistering_onRegisteringInvoked()
      throws ImsException {
    RegistrationCallback registrationCallback = mock(RegistrationCallback.class);
    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    shadowImsMmTelManager.setImsRegistering(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    verify(registrationCallback).onRegistering(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsRegistering(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  public void registerImsRegistrationCallback_imsRegistered_onRegisteredInvoked()
      throws ImsException {
    RegistrationCallback registrationCallback = mock(RegistrationCallback.class);
    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    shadowImsMmTelManager.setImsRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    verify(registrationCallback).onRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  public void registerImsRegistrationCallback_imsUnregistered_onUnregisteredInvoked()
      throws ImsException {
    RegistrationCallback registrationCallback = mock(RegistrationCallback.class);
    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    ImsReasonInfo imsReasonInfoWithCallbackRegistered = new ImsReasonInfo();
    shadowImsMmTelManager.setImsUnregistered(imsReasonInfoWithCallbackRegistered);

    verify(registrationCallback).onUnregistered(imsReasonInfoWithCallbackRegistered);

    ImsReasonInfo imsReasonInfoAfterUnregisteringCallback = new ImsReasonInfo();
    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsUnregistered(imsReasonInfoAfterUnregisteringCallback);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  public void registerImsRegistrationCallback_imsNotSupported_imsExceptionThrown() {
    shadowImsMmTelManager.setImsAvailableOnDevice(false);
    try {
      shadowImsMmTelManager.registerImsRegistrationCallback(
          Runnable::run, mock(RegistrationCallback.class));
      assertWithMessage("Expected ImsException was not thrown").fail();
    } catch (ImsException e) {
      assertThat(e.getCode()).isEqualTo(ImsException.CODE_ERROR_UNSUPPORTED_OPERATION);
      assertThat(e).hasMessageThat().contains("IMS not available on device.");
    }
  }

  @Test
  public void
      registerMmTelCapabilityCallback_imsRegistered_availabilityChange_onCapabilitiesStatusChangedInvoked()
          throws ImsException {
    MmTelCapabilities[] mmTelCapabilities = new MmTelCapabilities[1];
    CapabilityCallback capabilityCallback = new CapabilityCallback() {
          @Override
          public void onCapabilitiesStatusChanged(MmTelCapabilities capabilities) {
            super.onCapabilitiesStatusChanged(capabilities);
            mmTelCapabilities[0] = capabilities;
          }
        };

    shadowImsMmTelManager.setImsRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);
    shadowImsMmTelManager.registerMmTelCapabilityCallback(Runnable::run, capabilityCallback);

    MmTelCapabilities mmTelCapabilitiesWithCallbackRegistered = new MmTelCapabilities();
    mmTelCapabilitiesWithCallbackRegistered.addCapabilities(
        MmTelCapabilities.CAPABILITY_TYPE_VIDEO);
    mmTelCapabilitiesWithCallbackRegistered.addCapabilities(
        MmTelCapabilities.CAPABILITY_TYPE_VOICE);
    shadowImsMmTelManager.setMmTelCapabilitiesAvailable(mmTelCapabilitiesWithCallbackRegistered);

    assertThat(mmTelCapabilities[0]).isNotNull();
    assertThat(mmTelCapabilities[0]).isEqualTo(mmTelCapabilitiesWithCallbackRegistered);

    shadowImsMmTelManager.unregisterMmTelCapabilityCallback(capabilityCallback);
    shadowImsMmTelManager.setMmTelCapabilitiesAvailable(new MmTelCapabilities());
    assertThat(mmTelCapabilities[0]).isEqualTo(mmTelCapabilitiesWithCallbackRegistered);
  }

  @Test
  public void
      registerMmTelCapabilityCallback_imsNotRegistered_availabilityChange_onCapabilitiesStatusChangedNotInvoked()
          throws ImsException {
    MmTelCapabilities[] mmTelCapabilities = new MmTelCapabilities[1];
    CapabilityCallback capabilityCallback = new CapabilityCallback() {
          @Override
          public void onCapabilitiesStatusChanged(MmTelCapabilities capabilities) {
            super.onCapabilitiesStatusChanged(capabilities);
            mmTelCapabilities[0] = capabilities;
          }
        };

    shadowImsMmTelManager.registerMmTelCapabilityCallback(Runnable::run, capabilityCallback);
    shadowImsMmTelManager.setMmTelCapabilitiesAvailable(new MmTelCapabilities());

    assertThat(mmTelCapabilities[0]).isNull();
  }

  @Test
  public void registerMmTelCapabilityCallback_imsNotSupported_imsExceptionThrown() {
    shadowImsMmTelManager.setImsAvailableOnDevice(false);
    try {
      shadowImsMmTelManager.registerMmTelCapabilityCallback(
          Runnable::run, new CapabilityCallback());
      assertWithMessage("Expected ImsException was not thrown").fail();
    } catch (ImsException e) {
      assertThat(e.getCode()).isEqualTo(ImsException.CODE_ERROR_UNSUPPORTED_OPERATION);
      assertThat(e).hasMessageThat().contains("IMS not available on device.");
    }
  }
}