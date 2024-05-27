<template>
  <v-container fluid>
    <ValidationForm
      ref="observer"
      v-slot="{ meta, handleSubmit, validate }"
    >
      <v-row>
        <v-col cols="8">
          <!-- Name !-->
          <Field
            v-slot="{ field, errors }"
            v-model="newServiceOffering.name"
            name="Service Name"
            :rules="required"
          >
            <v-text-field
              id="serviceNameInput"
              v-bind="field"
              label="Name"
              variant="outlined"
              required
              density="compact"
              :error-messages="errors"

            />
          </Field>

          <!-- Category !-->
          <Field
            v-slot="{ field, errors }"
            v-model="newServiceOffering.serviceCategoryId"
            name="Category"
            :rules="required"
          >
            <v-select
              v-bind="field"
              :items="serviceOfferingCategories"
              item-title="name"
              item-value="id"
              label="Service Category"
              variant="outlined"
              density="compact"
              :error-messages="errors"

            />
            <span>{{ errors[0] }}</span>
          </Field>

          <!-- Short Description !-->
          <Field
            v-slot="{ field, errors }"
            v-model="newServiceOffering.shortDescription"
            name="Short Description"
            :rules="required"
            id="shortDescriptionInput2"
          >
            <v-text-field
              id="shortDescriptionInput"
              v-bind="field"
              label="Short Description"
              variant="outlined"
              density="compact"
              :error-messages="errors"

            />
          </Field>

          <!-- Description !-->
          <Field
            v-slot="{ field, errors }"
            v-model="newServiceOffering.description"
            name="Description"
            :rules="required"
          >
            <v-text-field
              id="descriptionInput"
              v-bind="field"
              label="Description"
              variant="outlined"
              density="compact"
              :error-messages="errors"

            />
          </Field>

          <!-- Cover Image !-->
          <v-file-input
            id="coverImageInput"
            v-model="uploadedServiceOfferingImage"
            label="Click here to select image"
            auto-grow
            density="compact"
            variant="outlined"
            @update:modelValue="loadServiceOfferingImage"
          />
        </v-col>
        <v-col cols="4">
          <service-offering-card-grid
            :service-offering="newServiceOffering"
            :passive="true"
            :create-or-edit-mode="true"
            :show-only-latest-version="true"
          />
        </v-col>
      </v-row>

      <!-- Navigation Buttons-->
      <v-card-actions>
        <v-btn
          :color="$vuetify.theme.themes.light.secondary"
          @click="$emit('step-canceled', stepNumber)"
        >
          {{ $t("buttons.Cancel") }}
        </v-btn>
        <v-spacer />
        <v-btn
          :color="
            !meta.valid
              ? $vuetify.theme.disable
              : $vuetify.theme.themes.light.secondary
          "
          @click="!meta.valid ? validate() : handleSubmit(onNextButtonClicked)"
        >
          <div v-if="editMode">Update</div>
          <div v-else>Create</div>
        </v-btn>
      </v-card-actions>
    </ValidationForm>
  </v-container>
</template>

<script>
import ServiceOfferingCardGrid from "@/components/service_offerings/ServiceOfferingCardGrid";
import { mapGetters } from "vuex";
import {Field, Form as ValidationForm } from "vee-validate";
import * as yup from 'yup';

export default {
  name: "ServiceOfferingWizardManualStep1Common",
  components: { ServiceOfferingCardGrid, Field, ValidationForm },
  props: ["editMode", "newServiceOffering"],
  setup(){
    const required = yup.string().required();
    return {
      required
    }
  },
  data() {
    return {
      stepNumber: 1,
      uploadedServiceOfferingImage: null,
    };
  },
  computed: {
    ...mapGetters([
      "serviceOfferingCategories",
      "serviceOfferingDeploymentTypes",
    ]),
  },
  methods: {
    loadServiceOfferingImage(files) {
      if (!this.uploadedServiceOfferingImage) {
        console.log("No File Chosen");
      } else {
        const reader = new FileReader();
        reader.readAsDataURL(this.uploadedServiceOfferingImage);
        reader.onload = () => {
          this.newServiceOffering.coverImage = reader.result;
        };
      }
    },
    async onNextButtonClicked() {
      this.$emit("step-completed", this.stepNumber);
    },
  },
};
</script>
