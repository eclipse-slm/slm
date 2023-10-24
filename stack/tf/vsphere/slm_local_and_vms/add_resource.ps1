$slm_hostname = $args[0]
$slm_username = $args[1]
$slm_password = $args[2]
$vm_username = $args[3]
$vm_password = $args[4]
$vm_ip_address = $args[5]

# Get Keycloak Access Token:
$kc_access_token = curl.exe --location "http://$($slm_hostname):7080/auth/realms/fabos/protocol/openid-connect/token" --header "Content-Type: application/x-www-form-urlencoded" --data-urlencode "username=$($slm_username)" --data-urlencode "password=$($slm_password)" --data-urlencode "client_id=ui" --data-urlencode "grant_type=password" -s | jq -r ".access_token"

# Add resource to resource-registry:
curl.exe -X "PUT" "http://$($slm_hostname):9010/resources?resourceUsername=$($vm_username)&resourcePassword=$($vm_password)&resourceHostname=$($vm_ip_address)&resourceIp=$($vm_ip_address)&resourceConnectionType=ssh&resourceConnectionPort=22" -H "Realm: fabos" -H "Authorization: Bearer $($kc_access_token)"
