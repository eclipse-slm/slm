<template>
  <ValidationForm
    ref="observer"
    v-slot="{ meta, handleSubmit, validate }"
  >
    <div
      v-for="serviceOptionCategory in serviceOfferingVersion.serviceOptionCategories"
      :key="serviceOptionCategory.id"
    >
      <base-material-card color="secondary">
        <template #heading>
          <v-row class="my-1">
            <v-btn
              icon="mdi-arrow-up"
              variant="text"
              size="small"
              class="mx-2"
              @click="
                onMoveServiceOptionCategoryUpClicked(serviceOptionCategory.id)
              "
            />
            <v-btn
              icon="mdi-arrow-down"
              variant="text"
              size="small"
              @click="
                onMoveServiceOptionCategoryDownClicked(serviceOptionCategory.id)
              "
            />
            <v-text-field
              v-model="serviceOptionCategory.name"
              class="mx-6"
              density="compact"
            />
            <v-btn
              icon="mdi-delete"
              variant="text"
              size="small"
              class="mx-4"
              @click="
                onServiceOptionCategoryDeleteClicked(serviceOptionCategory.id)
              "
            />
          </v-row>
        </template>
        <v-card-text>
          <service-options-definition-table
            :service-option-category="serviceOptionCategory"
          />
        </v-card-text>
      </base-material-card>
    </div>

    <v-row>
      <v-col>
        <v-btn
          v-if="serviceOfferingVersion.serviceOptionCategories.length > 0"
          color="secondary"
          @click="onNewServiceOptionCategoryClicked"
        >
          <v-icon>mdi-plus</v-icon>
          Service Option Category
        </v-btn>
        <div v-else>
          No service options defined in previous steps
        </div>
      </v-col>
    </v-row>

    <!-- Navigation Buttons-->
    <v-card-actions>
      <v-btn
        variant="elevated"
        :color="$vuetify.theme.themes.light.colors.secondary"
        @click="$emit('step-canceled', stepNumber)"
      >
        {{ $t("buttons.Back") }}
      </v-btn>
      <v-spacer />
      <v-btn
        variant="elevated"
        :color="
          !meta.valid
            ? $vuetify.theme.themes.light.colors.disable
            : $vuetify.theme.themes.light.colors.secondary
        "
        @click="!meta.valid ? validate() : handleSubmit(emitStepCompleted)"
      >
        {{ $t("buttons.Next") }}
      </v-btn>
    </v-card-actions>
  </ValidationForm>
</template>

<script>
import {serviceOptionMixin} from "@/utils/serviceOptionUtil";
import serviceOptionsDefinitionTable
  from "@/components/service_offerings/wizard_service_offering_version/serviceOptions/serviceOptionsDefinitionTable";
import {Form as ValidationForm} from "vee-validate";

export default {
  name: "ServiceOfferingVersionWizardStep3ServiceOptions",
  components: {
    serviceOptionsDefinitionTable,
    ValidationForm
  },
  mixins: [serviceOptionMixin],
  props: {
    editMode: {
      type: Boolean,
      default: false
    },
    serviceOfferingVersion: {
      type: Object,
      default: null
    },
  },
  data() {
    return {
      stepNumber: 3,
    };
  },
  methods: {
    onNewServiceOptionCategoryClicked() {
      this.serviceOfferingVersion.serviceOptionCategories.push({
        id: this.serviceOfferingVersion.serviceOptionCategories.length + 1,
        name: "New Service Option Category",
        serviceOptions: [],
      });
    },
    onServiceOptionCategoryDeleteClicked(serviceOptionCategoryId) {
      const clickedCategoryIndex =
        this.serviceOfferingVersion.serviceOptionCategories.findIndex(
          (item) => item.id === serviceOptionCategoryId
        );
      if (
        this.serviceOfferingVersion.serviceOptionCategories[
          clickedCategoryIndex
        ].serviceOptions?.length === 0
      ) {
        this.serviceOfferingVersion.serviceOptionCategories.splice(
          clickedCategoryIndex,
          1
        );
      } else {
        this.$toast.warning(
          `A category that contains service options cannot be deleted`
        );
      }
    },
    onMoveServiceOptionCategoryUpClicked(serviceOptionCategoryId) {
      const clickedCategoryIndex =
        this.serviceOfferingVersion.serviceOptionCategories.findIndex(
          (item) => item.id === serviceOptionCategoryId
        );
      if (clickedCategoryIndex > 0) {
        this.swap(
          this.serviceOfferingVersion.serviceOptionCategories,
          clickedCategoryIndex,
          clickedCategoryIndex - 1
        );
        this.$forceUpdate();
      }
    },
    onMoveServiceOptionCategoryDownClicked(serviceOptionCategoryId) {
      const clickedCategoryIndex =
        this.serviceOfferingVersion.serviceOptionCategories.findIndex(
          (item) => item.id === serviceOptionCategoryId
        );
      if (
        clickedCategoryIndex <
        this.serviceOfferingVersion.serviceOptionCategories.length - 1
      ) {
        this.swap(
          this.serviceOfferingVersion.serviceOptionCategories,
          clickedCategoryIndex,
          clickedCategoryIndex + 1
        );
        this.$forceUpdate();
      }
    },
    swap(input, indexA, indexB) {
      const temp = input[indexA];

      input[indexA] = input[indexB];
      input[indexB] = temp;
    },
    emitStepCompleted() {
      this.$emit("step-completed", this.stepNumber);
    },
  },
};
</script>
