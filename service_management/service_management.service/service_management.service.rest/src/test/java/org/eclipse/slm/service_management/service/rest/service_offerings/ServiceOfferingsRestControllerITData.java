package org.eclipse.slm.service_management.service.rest.service_offerings;

public class ServiceOfferingsRestControllerITData {

    public static final String serviceOfferingExists_expectedAwxJobLaunchRequestBody = """
        {
          "extra_vars":{
            "resource_id":"11a8ada6-0f00-4e70-a41d-7d562c6e24a4",
            "docker_compose_file":{
              "version":"3",
              "services":{
                "service":{
                  "image":"nginx:latest",
                  "restart":"always",
                  "ports":["5555:5555","3333:6666"],
                  "environment":["envVarButNotServiceOption2=val3",
                    "envVar1=value1","envVarButNotServiceOption1=val3",
                    "envVar2=value2"],
                  "volumes":[
                    "data:/sample/path/data","config:/sample/path/config"
                  ],
                  "labels":["label1=labelVal1",
                    "label2=labelVal2"]
                }
              },
              "volumes":{
                "data":{
                  "driver":
                  "local"
                },
                "config":{
                  "driver":"local"
                }
              }
            }
          }
        }
        """;
}
