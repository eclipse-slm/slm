## Requirements

### Run SLM on remote target host

Clone SLM from https://github.com/eclipse-slm/slm on remote host and start slm with docker-compose from 

`stack/compose`

or run terraform scenario from 

`stack/tf/slm_local_and_vms`

### Windows target hosts

#### SSH
Install ssh server
````powershell
Get-WindowsCapability -Online | Where-Object Name -like 'OpenSSH*'

Add-WindowsCapability -Online -Name OpenSSH.Server~~~~0.0.1.0

Start-Service sshd

Set-Service -Name sshd -StartupType 'Automatic'

if (!(Get-NetFirewallRule -Name "OpenSSH-Server-In-TCP" -ErrorAction SilentlyContinue | Select-Object Name, Enabled)) {
    Write-Output "Firewall Rule 'OpenSSH-Server-In-TCP' does not exist, creating it..."
    New-NetFirewallRule -Name 'OpenSSH-Server-In-TCP' -DisplayName 'OpenSSH Server (sshd)' -Enabled True -Direction Inbound -Protocol TCP -Action Allow -LocalPort 22
} else {
    Write-Output "Firewall rule 'OpenSSH-Server-In-TCP' has been created and exists."
}
````

#### Rights on target host
Make sure you use a local user with admin rights or appropriate rights to read files from 

## How to run

### 1. Set environment Variables

Docker-compose requires the following env vars to work

| Name             | Example     | CMD (win)                           | Description                                                                                                                                     |
|------------------|-------------|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| ANSIBLE_HOST     | 192.168.1.1 | $env:ANSIBLE_HOST = '192.168.1.1'   | Hostname/IP of host the slm is running on                                                                                                       |
| ANSIBLE_USER     | slm-user    | $env:ANSIBLE_USER = 'slm-user'      | Windows/Linux username of SLM-host                                                                                                              |
| ANSIBLE_PASSWORD | pa$$w0rd    | $env:ANSIBLE_PASSWORD = 'pa$$w0rd'  | Password of Windows/Linux user                                                                                                                  |
| PROJECT_FOLDER   | C:\code\slm | $env:PROJECT_FOLDER = 'C:\code\slm' | Project root folder on SLM-host (cloned from https://github.com/eclipse-slm/slm). Windows/Linux user must have read priviligaes for this folder |
| LOCAL_HOST       | 192.168.1.2 | $env:ANSIBLE_HOST = '192.168.1.2'   | IP address of host the playbooks will run on (NO hostname)                                                                                      |

### 2. Run remote-config-expose container

````shell
docker compose up -d --build --force-recreate
````
### 3. Run playbook inside container

````shell
docker exec -it remote_config_exporter-remote-config-exporter-1 bash
ansible-playbook -i inventory.yml main.yml
````

or

````shell
docker exec -it remote_config_exporter-remote-config-exporter-1 ansible-playbook -i inventory.yml main.yml
````