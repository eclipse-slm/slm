<template>
  <div>
    <validation-observer ref="observer" v-slot="{ invalid, handleSubmit, validate }">
      <div v-for="requirement in serviceOfferingVersion.serviceRequirements" :key="requirement.id">
        <base-material-card color="secondary">
          <template #heading>
            <v-row class="my-1">
              <v-text-field v-model="requirement.name" class="mx-6" dense />
              <v-btn icon small class="mx-4" @click="onRequirementDeleteClicked(requirement)">
                <v-icon>
                  mdi-delete
                </v-icon>
              </v-btn>
            </v-row>
          </template>
          <v-tabs
            v-model="requirement.activeLogic"
          >
            <v-tab v-for="logic in requirement.logics" :key="logic.id" :value="logic.id">
              <v-select v-model="logic.type" :items="logicList" dense />
              <v-btn icon small class="mx-4" @click="onLogicDeleteClicked(requirement, logic)">
                <v-icon>
                  mdi-delete
                </v-icon>
              </v-btn>
            </v-tab>
            <v-btn color="secondary" @click="onNewLogicClicked(requirement)">
                  <v-icon>mdi-plus</v-icon>
                  {{ $t('buttons.AddLogic') }}
                </v-btn>
          </v-tabs>
          <v-card-text>
            <v-window v-model="requirement.activeLogic">
              <v-window-item v-for="logic in requirement.logics" :key="logic.id" :value="logic.id">
                <v-card v-for="property in logic.properties" :key="property.id" class="mt-0">
                  <v-card-title>
                    <v-text-field v-model="property.name" dense />
                    <v-btn icon small class="mx-4" @click="onPropertyDeleteClicked(logic, property)">
                      <v-icon>
                        mdi-delete
                      </v-icon>
                    </v-btn>
                  </v-card-title>
                  <v-card-text>
                    <v-row>
                      <v-col>
                        <v-text-field v-model="property.semanticId" :label="$t('RequirementLabels.PropertySemanticId')" />
                      </v-col>
                      <v-col>
                        <v-text-field v-model="property.value" :label="$t('RequirementLabels.PropertyValue')" />
                      </v-col>
                    </v-row>
                    <v-combobox v-model="property.parentSubmodelsSemanticIds" :label="$t('RequirementLabels.ParentSubmodelsSemanticIds')"
                      multiple>
                    </v-combobox>
                  </v-card-text>
                </v-card>
                <v-row>
                  <v-col>
                    <v-btn color="secondary" @click="onNewPropertyClicked(logic)">
                      <v-icon>mdi-plus</v-icon>
                      {{ $t('buttons.AddProperty') }}
                    </v-btn>
                  </v-col>
                </v-row>
              </v-window-item>
            </v-window>
          </v-card-text>
        </base-material-card>
      </div>

      <v-row>
        <v-col>
          <v-btn color="secondary" @click="onNewRequirementClicked">
            <v-icon>mdi-plus</v-icon>
            {{ $t('buttons.AddRequirement') }}
          </v-btn>
        </v-col>
      </v-row>

      <!-- Navigation Buttons-->
      <v-card-actions>
        <v-btn :color="$vuetify.theme.themes.light.secondary" @click="$emit('step-canceled', stepNumber)">
          {{ $t('buttons.Back') }}
        </v-btn>
        <v-spacer />
        <v-btn :color="invalid ? $vuetify.theme.disable : $vuetify.theme.themes.light.secondary"
          @click="invalid ? validate() : handleSubmit(emitStepCompleted)">
          <div v-if="editMode">
            {{ $t('buttons.Update') }}
          </div>
          <div v-else>
            {{ $t('buttons.Create') }}
          </div>
        </v-btn>
      </v-card-actions>
    </validation-observer>
  </div>
</template>

<script>
import 'vue-json-pretty/lib/styles.css'

export default {
  name: 'ServiceOfferingVersionWizardStep4Requirements',
  components: {},
  props: ['editMode', 'serviceOfferingVersion'],

  data() {
    return {
      stepNumber: 4,
      logicList: [
        'ANY',
        'ALL',
      ],
    }
  },
  methods: {
    onNewRequirementClicked() {
      this.serviceOfferingVersion.serviceRequirements.push({
        id: this.serviceOfferingVersion.serviceRequirements.length,
        name: 'NewRequirement' + (this.serviceOfferingVersion.serviceRequirements.length + 1),
        logics: [],
        activeLogic: null
      })
    },
    onNewLogicClicked(requirement) {
      var newId = requirement.logics.length
      requirement.logics.push({
        id: newId,
        type: 'ANY',
        properties: [],
      })
      requirement.activeLogic = newId
    },
    onNewPropertyClicked(logic) {
      logic.properties.push({
        id: logic.properties.length,
        name: "NewProperty" + (logic.properties.length + 1),
        semanticId: "",
        parentSubmodelsSemanticIds: [],
        value: "",
      })
    },
    onRequirementDeleteClicked(requirement) {
      this.serviceOfferingVersion.serviceRequirements = this.serviceOfferingVersion.serviceRequirements.filter((r) => r !== requirement)
    },
    onLogicDeleteClicked(requirement, logic) {
      requirement.logics = requirement.logics.filter((l) => l !== logic)
    },
    onPropertyDeleteClicked(logic, property) {
      logic.properties = logic.properties.filter((p) => p !== property)
    },
    emitStepCompleted () {
      console.log(this.serviceOfferingVersion.serviceRequirements)
      this.$emit('step-completed', this.stepNumber)
    },

  }
}
</script>
