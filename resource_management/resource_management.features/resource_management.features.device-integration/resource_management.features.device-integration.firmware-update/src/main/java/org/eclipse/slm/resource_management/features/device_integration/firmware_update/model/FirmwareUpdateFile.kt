package org.eclipse.slm.resource_management.features.device_integration.firmware_update.model

import java.time.ZonedDateTime

class FirmwareUpdateFile(

    var fileName: String,

    var fileSizeBytes: Long,

    var uploadDate: ZonedDateTime,
)
{
    var downloadUrl: String = ""
}