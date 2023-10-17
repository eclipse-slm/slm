<template>
  <v-container fluid>
    <validation-observer
      ref="observer"
      v-slot="{ invalid, handleSubmit, validate }"
    >
      <v-row>
        <v-col cols="8">
          <!-- Name !-->
          <validation-provider
            v-slot="{ errors, valid }"
            name="Service Name"
            rules="required"
          >
            <v-text-field
              id="serviceNameInput"
              v-model="newServiceOffering.name"
              label="Name"
              outlined
              required
              dense
              :error-messages="errors"
              :success="valid"
            />
          </validation-provider>

          <!-- Category !-->
          <validation-provider
            v-slot="{ errors, valid }"
            name="Category"
            rules="required"
          >
            <v-select
              v-model="newServiceOffering.serviceCategoryId"
              :items="serviceOfferingCategories"
              item-text="name"
              item-value="id"
              label="Service Category"
              outlined
              dense
              :error-messages="errors"
              :success="valid"
            />
            <span>{{ errors[0] }}</span>
          </validation-provider>

          <!-- Short Description !-->
          <validation-provider
            v-slot="{ errors, valid }"
            name="Short Description"
            rules="required"
            id="shortDescriptionInput2"
          >
            <v-text-field
              id="shortDescriptionInput"
              v-model="newServiceOffering.shortDescription"
              label="Short Description"
              outlined
              dense
              :error-messages="errors"
              :success="valid"
            />
          </validation-provider>

          <!-- Description !-->
          <validation-provider
            v-slot="{ errors, valid }"
            name="Description"
            rules="required"
          >
            <v-text-field
              id="descriptionInput"
              v-model="newServiceOffering.description"
              label="Description"
              outlined
              dense
              :error-messages="errors"
              :success="valid"
            />
          </validation-provider>

          <!-- Cover Image !-->
          <v-file-input
            id="coverImageInput"
            v-model="uploadedServiceOfferingImage"
            label="Click here to select image"
            auto-grow
            dense
            outlined
            @change="loadServiceOfferingImage"
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
            invalid
              ? $vuetify.theme.disable
              : $vuetify.theme.themes.light.secondary
          "
          @click="invalid ? validate() : handleSubmit(onNextButtonClicked)"
        >
          <div v-if="editMode">Update</div>
          <div v-else>Create</div>
        </v-btn>
      </v-card-actions>
    </validation-observer>
  </v-container>
</template>

<script>
import ServiceOfferingCardGrid from "@/components/service_offerings/ServiceOfferingCardGrid";
import { mapGetters } from "vuex";

export default {
  name: "ServiceOfferingWizardManualStep1Common",
  components: { ServiceOfferingCardGrid },
  props: ["editMode", "newServiceOffering"],
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
