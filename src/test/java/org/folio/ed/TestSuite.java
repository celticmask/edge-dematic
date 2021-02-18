package org.folio.ed;

import org.folio.ed.integration.EmsIntegrationTest;
import org.folio.ed.integration.StagingDirectorIntegrationTest;
import org.folio.ed.service.SecurityManagerServiceTest;
import org.folio.ed.util.StagingDirectorStatusHelperTest;
import org.junit.jupiter.api.Nested;

public class TestSuite {
  @Nested
  class SecurityManagerServiceTestNested extends SecurityManagerServiceTest {
  }

  @Nested
  class StagingDirectorIntegrationTestNested extends StagingDirectorIntegrationTest {
  }

  @Nested
  class EmsIntegrationTestNested extends EmsIntegrationTest {
  }

  @Nested
  class StagingDirectorStatusHelperTestNested extends StagingDirectorStatusHelperTest {
  }
}
