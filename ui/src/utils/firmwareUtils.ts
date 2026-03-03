import axios from "axios";

const SIZE_UNITS = ["B", "kB", "MB", "GB", "TB"];

export function humanFileSize(size: number): string {
  const base = size === 0 ? 0 : Math.floor(Math.log(size) / Math.log(1024));
  const value = +(size / Math.pow(1024, base)).toFixed(2);
  return `${value} ${SIZE_UNITS[base]}`;
}

export function downloadItem(item: { downloadUrl: string; fileName: string }): void {
  axios
    .get(item.downloadUrl, { responseType: "blob" })
    .then((response) => {
      const blob = new Blob([response.data]);
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = item.fileName;
      link.click();
      URL.revokeObjectURL(link.href);
    })
    .catch(console.error);
}

export function getVersionTextOfSoftwareNameplate(
  availableFirmwareVersions: Array<{ softwareNameplateSubmodelId?: string; version?: string }> = [],
  softwareNameplateSubmodelId?: string | null
): string {
  const version = availableFirmwareVersions.find(
    (firmwareVersion) =>
      firmwareVersion.softwareNameplateSubmodelId?.trim() ===
      softwareNameplateSubmodelId?.trim()
  )?.version;
  return version ?? "N/A";
}
