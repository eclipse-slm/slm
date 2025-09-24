{{ with secret "pki_int_slm/issue/traefik" (printf "common_name=%s" (env "SLM_HOSTNAME")) }}
{{ .Data.private_key }}
{{ .Data.certificate }}
{{- range .Data.ca_chain }}
{{ . }}
{{- end }}
{{ end }}
