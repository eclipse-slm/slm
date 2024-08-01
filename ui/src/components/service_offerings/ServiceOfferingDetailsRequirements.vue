<template>
  <v-container>
    <v-row>
      <base-material-card
        v-for="requirement in requirements"
        :key="requirement.id"
        flat
        width="100%"
        color="secondary"
      >
        <template #heading>
          {{ requirement.name }}
        </template>
        <div
          v-for="logic in requirement.logics"
          :key="logic.id"
          class="mt-0"
        >
          <v-card-title>
            {{ logic.type }}
          </v-card-title>
          <v-card-text>
            <v-card
              v-for="property in logic.properties"
              :key="property.id"
              class="mt-0"
            >
              <v-card-title>
                {{ property.name }}
              </v-card-title>
              <v-card-text>
                <v-row>
                  <v-col>
                    {{ $t('RequirementLabels.PropertySemanticId') }}: <code>{{ property.semanticId }}</code>
                  </v-col>
                  <v-col>
                    {{ $t('RequirementLabels.PropertyValue') }}: <code>{{ property.value }}</code>
                  </v-col>
                </v-row>
                {{ $t('RequirementLabels.ParentSubmodelsSemanticIds') }}: <code>{{ property.parentSubmodelsSemanticIds.join(", ") }}</code>
              </v-card-text>
            </v-card>
          </v-card-text>
        </div>
      </base-material-card>
    </v-row>
  </v-container>
</template>

<script>
import ServiceOfferingVersionsRestApi from '@/api/service-management/serviceOfferingVersionsRestApi'

export default {
    name: 'ServiceDetailsRequirements',
    props: {
      serviceOffering: { type: Object },
      serviceOfferingVersionId: { type: String }
    },
    data () {
      return {
        requirements: [],
      }
    },
    watch: {
      serviceOfferingVersionId: function(newId, oldId) {
        this.updateRequirements()
      }
    },
    mounted () {
      this.updateRequirements()
    },
    methods: {
      updateRequirements() {
        if (!this.serviceOfferingVersionId) {
          return
        }
        ServiceOfferingVersionsRestApi.getServiceOfferingVersionById(this.serviceOffering.id, this.serviceOfferingVersionId)
          .then(serviceOfferingVersion => {
            this.requirements = serviceOfferingVersion.serviceRequirements
          })
      }
    }
  }
</script>

<style scoped>

</style>
