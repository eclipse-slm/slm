$slm_hostname         = $args[0]
$slm_username         = $args[1]
$slm_password         = $args[2]
$vm_username          = $args[3]
$vm_password          = $args[4]
$vm_ip_address        = $args[5]
$default_slm_hostname = "<insert-slm-ip-here>"
$slm_host_reachable   = Test-Connection -count 1 -quiet $slm_hostname

if( $slm_hostname  -eq $default_slm_hostname ) {
  echo "SLM hostname equals default value => Skip adding VMs to SLM instance..."
  exit
}

if( -not($slm_host_reachable) ) {
  echo "SLM hostname not reachable => Skip adding VMs to SLM instance..."
  exit
}

$kc_status = curl.exe -s -m 2 -I "http://$($slm_hostname):7080/auth/realms/fabos/.well-known/openid-configuration"

if( -not($LASTEXITCODE -eq 0) ) {
  echo "Keycloak of SLM not reachable => Skip adding VMs to SLM instance..."
  exit
}

# Get Keycloak Access Token:
$kc_access_token = curl.exe --location "http://$($slm_hostname):7080/auth/realms/fabos/protocol/openid-connect/token" --header "Content-Type: application/x-www-form-urlencoded" --data-urlencode "username=$($slm_username)" --data-urlencode "password=$($slm_password)" --data-urlencode "client_id=self-service-portal" --data-urlencode "grant_type=password" -s | jq -r ".access_token"

# Add resource to resource-registry:
curl.exe -X "PUT" "http://$($slm_hostname):9010/resources?resourceUsername=$($vm_username)&resourcePassword=$($vm_password)&resourceHostname=$($vm_ip_address)&resourceIp=$($vm_ip_address)&resourceConnectionType=ssh&resourceConnectionPort=22" -H "Realm: fabos" -H "Authorization: Bearer $($kc_access_token)"
