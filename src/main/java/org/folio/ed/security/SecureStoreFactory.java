package org.folio.ed.security;

import java.util.Properties;

import org.folio.edge.core.security.AwsParamStore;
import org.folio.edge.core.security.EphemeralStore;
import org.folio.edge.core.security.SecureStore;
import org.folio.edge.core.security.VaultStore;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecureStoreFactory {

  private SecureStoreFactory() {
  }

  public static SecureStore getSecureStore(String type, Properties props) {
    SecureStore ret;



    if (type == null)
      type = "";

    switch (type) {
    case VaultStore.TYPE:
      ret = new VaultStore(props);
      break;
    case AwsParamStore.TYPE:
      ret = new TenantAwareAWSParamStore(props);
      break;
    case EphemeralStore.TYPE:
    default:
      ret = new EphemeralStore(props);
    }

    log.info("type: {}, class: {}", type, ret.getClass().getName());
    return ret;
  }

}
