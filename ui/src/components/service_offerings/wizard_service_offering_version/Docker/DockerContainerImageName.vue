<template>
  <v-row>
    <v-col>
      <v-row>
        <v-col cols="3">
          <v-subheader>
            Image Repository
            <v-tooltip bottom>
              <template #activator="{ on, attrs }">
                <v-icon
                  class="mx-3"
                  color="secondary"
                  dark
                  v-bind="attrs"
                  @click.stop="ImageDialog = true"
                  v-on="on"
                >
                  mdi-information
                </v-icon>
              </template>
              <span>Image repository of the Docker image</span>
            </v-tooltip>
          </v-subheader>
        </v-col>

        <v-col cols="9">
          <ValidationProvider
            v-slot="{ errors, valid }"
            name="Image Repository"
            rules="required"
          >
            <v-text-field
              v-model="newServiceOffering.deploymentDefinition.imageRepository"
              class="full-width"
              placeholder="e.g. registry:port/myImage"
              required
              dense
              clearable
              :error-messages="errors"
              :success="valid"
            />
          </ValidationProvider>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="3">
          <v-subheader>
            Image Tag
            <v-tooltip bottom>
              <template #activator="{ on, attrs }">
                <v-icon
                  class="mx-3"
                  color="secondary"
                  dark
                  v-bind="attrs"
                  @click.stop="ImageDialog = true"
                  v-on="on"
                >
                  mdi-information
                </v-icon>
              </template>
              <span>Tag of the Docker image</span>
            </v-tooltip>
          </v-subheader>
        </v-col>

        <v-col cols="9">
          <ValidationProvider
            v-slot="{ errors, valid }"
            name="Image Tag"
            rules="required"
          >
            <v-text-field
              v-model="newServiceOffering.deploymentDefinition.imageTag"
              class="full-width"
              placeholder="e.g. latest"
              required
              dense
              clearable
              :error-messages="errors"
              :success="valid"
            />
          </ValidationProvider>
        </v-col>
      </v-row>
    </v-col>
  </v-row>
</template>

<script>
  export default {
    name: 'DockerContainerImageName',
    comments: {
    },
    props: ['newServiceOffering'],
  }
</script>
