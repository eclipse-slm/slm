<template>
  <v-row>
    <v-col>
      <v-row>
        <v-col cols="3">
          <v-list-subheader>
            Image Repository
            <v-tooltip location="bottom">
              <template #activator="{ props }">
                <v-icon
                  class="mx-3"
                  color="secondary"
                  theme="dark"
                  v-bind="props"
                  @click.stop="ImageDialog = true"
                >
                  mdi-information
                </v-icon>
              </template>
              <span>Image repository of the Docker image</span>
            </v-tooltip>
          </v-list-subheader>
        </v-col>

        <v-col cols="9">
          <Field
            v-slot="{ field, errors }"
            v-model="newServiceOffering.deploymentDefinition.imageRepository"
            name="Image Repository"
            :rules="required"
          >
            <v-text-field
              v-bind="field"
              class="full-width"
              placeholder="e.g. registry:port/myImage"
              required
              density="compact"
              clearable
              :error-messages="errors"
              :model-value="newServiceOffering.deploymentDefinition.imageRepository"
            />
          </Field>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="3">
          <v-list-subheader>
            Image Tag
            <v-tooltip location="bottom">
              <template #activator="{ props }">
                <v-icon
                  class="mx-3"
                  color="secondary"
                  theme="dark"
                  v-bind="props"
                  @click.stop="ImageDialog = true"
                >
                  mdi-information
                </v-icon>
              </template>
              <span>Tag of the Docker image</span>
            </v-tooltip>
          </v-list-subheader>
        </v-col>

        <v-col cols="9">
          <Field
            v-slot="{ field, errors }"
            v-model="newServiceOffering.deploymentDefinition.imageTag"
            name="Image Tag"
            :rules="required"
          >
            <v-text-field
              v-bind="field"
              class="full-width"
              placeholder="e.g. latest"
              required
              density="compact"
              clearable
              :error-messages="errors"
              :model-value="newServiceOffering.deploymentDefinition.imageTag"
            />
          </Field>
        </v-col>
      </v-row>
    </v-col>
  </v-row>
</template>

<script>
import {Field} from "vee-validate";
import * as yup from 'yup';

export default {
    name: 'DockerContainerImageName',
    comments: {
    },
    components: {Field},
    props: ['newServiceOffering'],
    setup(){
      const required = yup.string().required()
      return {
        required
      }
    },
  }
</script>
