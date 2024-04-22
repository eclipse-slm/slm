<template>
  <div>
    <v-data-table
      :footer-props="{
        'items-per-page-options': [5, 10, 20, -1],
        pageText: $t('vuetify.dataIterator.pageText'),
        'items-per-page-text': $t('vuetify.dataIterator.itemsPerPageText'),
        'items-per-page-all-text': $t('vuetify.dataIterator.itemsPerPageAllText')
      }"
      :headers="ServiceOfferingsTableHeaders"
      :expanded="expanded"
      item-key="id"
      :items="serviceOfferings"
    >
      <template #item="{ item, isExpanded }">
        <tr @click="expandRow(item)">
          <td>{{ item.name }}</td>
          <td>{{ item.id }}</td>
          <td>{{ serviceOfferingCategoryNameById(item.serviceCategoryId) }}</td>
          <td>
            <v-row>
              <v-tooltip
                location="bottom"
                :disabled="!item.scmBased"
              >
                <template #activator="{ props }">
                  <div v-bind="props">
                    <v-btn
                      :disabled="item.scmBased"
                      class="ma-1"
                      color="accent"
                      size="small"
                      @click="onAddServiceOfferingVersionClicked(item)"
                    >
                      <v-icon>
                        mdi-plus
                      </v-icon>
                    </v-btn>
                  </div>
                </template>
                <span>This action is only available for service offerings that are not based on a Git repository</span>
              </v-tooltip>
              <v-tooltip
                location="bottom"
                :disabled="!item.scmBased"
              >
                <template #activator="{ props }">
                  <div v-bind="props">
                    <v-btn
                      id="editServiceOfferingButton"
                      class="ma-1"
                      size="small"
                      color="info"
                      :disabled="item.scmBased"
                      @click.stop="onEditServiceOfferingClicked(item)"
                    >
                      <v-icon>
                        mdi-pencil
                      </v-icon>
                    </v-btn>
                  </div>
                </template>
                <span>This action is only available for service offerings that are not based on a Git repository</span>
              </v-tooltip>
              <v-btn
                class="ma-1"
                color="error"
                size="small"
                @click.stop="onDeleteServiceOfferingClicked(item)"
              >
                <v-icon>
                  mdi-delete
                </v-icon>
              </v-btn>
            </v-row>
          </td>
          <td>
            <v-icon v-if="item.scmBased">
              mdi-git
            </v-icon>
            <v-icon v-else>
              mdi-account
            </v-icon>
          </td>
          <td>
            <v-icon
              @click.stop="expandRow(item)"
            >
              {{ isExpanded ? 'mdi-close' : 'mdi-chevron-down' }}
            </v-icon>
          </td>
        </tr>
      </template>

      <template #expanded-item="{ headers }">
        <td :colspan="headers.length">
          <v-row
            v-if="expandedServiceOfferingVersions == undefined"
            align="center"
            justify="center"
          >
            <progress-circular
              width="5"
              size="50"
            />
          </v-row>
          <v-col
            v-else
            cols="12"
          >
            <v-data-table
              v-if="expandedServiceOfferingVersions.length > 0"
              :items="expandedServiceOfferingVersions"
              item-key="id"
              :headers="ServiceOfferingVersionsTableHeaders"
              sort-by="version"
              disable-pagination
              hide-default-footer
            >
              <template #item="{ item }">
                <tr>
                  <td>{{ item.version }}</td>
                  <td>{{ item.id }}</td>
                  <td>{{ new Date(item.created).toISOString().slice(0,10) }}</td>
                  <td>
                    <v-row>
                      <v-tooltip
                        location="bottom"
                        :disabled="!expandedServiceOffering.scmBased"
                      >
                        <template #activator="{ props }">
                          <div v-bind="props">
                            <v-btn
                              :disabled="expandedServiceOffering.scmBased"
                              class="ma-1"
                              color="info"
                              size="small"
                              @click="onEditServiceOfferingVersionClicked(item)"
                            >
                              <v-icon>
                                mdi-pencil
                              </v-icon>
                            </v-btn>
                          </div>
                        </template>
                        <span>This action is only available for service offerings that are not based on a Git repository</span>
                      </v-tooltip>

                      <v-tooltip
                        location="bottom"
                        :disabled="!expandedServiceOffering.scmBased"
                      >
                        <template #activator="{ props }">
                          <div v-bind="props">
                            <v-btn
                              :disabled="expandedServiceOffering.scmBased"
                              class="ma-1"
                              color="error"
                              size="small"
                              @click="onDeleteServiceOfferingVersionClicked(item)"
                            >
                              <v-icon>
                                mdi-delete
                              </v-icon>
                            </v-btn>
                          </div>
                        </template>
                        <span>This action is only available for service offerings that are not based on a Git repository</span>
                      </v-tooltip>
                    </v-row>
                  </td>
                </tr>
              </template>
            </v-data-table>

            <v-row v-else>
              <v-col>
                No version exists for this service offering
              </v-col>
              <v-col cols="9">
                <v-btn
                  color="secondary"
                  @click="onAddServiceOfferingVersionClicked(expandedServiceOffering)"
                >
                  <v-icon
                    density="compact"
                    size="small"
                    class="mr-2"
                  >
                    mdi-plus-circle
                  </v-icon>
                  Create first version
                </v-btn>
              </v-col>
            </v-row>
          </v-col>
        </td>
      </template>
    </v-data-table>
  </div>
</template>

<script>
import {mapGetters} from "vuex";
import ServiceOfferingVersionsRestApi from  "@/api/service-management/serviceOfferingVersionsRestApi";
import ProgressCircular from "@/components/base/ProgressCircular";

export default {
  name: "ServiceOfferingTable",
  components: {ProgressCircular},
  props: ['serviceOfferings'],
  data () {
    return {
      selectedServiceOffering: {},
      serviceOfferingVersionOfServiceOfferingId: {},
      expandedServiceOffering: undefined,
      expandedServiceOfferingVersions: undefined,
      expanded: [],
    }
  },
  mounted() {
    this.emitInterface();
  },
  computed: {
    ...mapGetters([
      'serviceOfferingCategoryNameById',
      'serviceVendorById',
    ]),
    ServiceOfferingsTableHeaders () {
      return [
        { text: 'Name', value: 'name', sortable: true, width: '15%' },
        { text: 'Id', value: 'id', sortable: true, width: '35%' },
        { text: 'Category', value: 'serviceCategory', sortable: true, width: '15%' },
        { text: 'Actions', value: 'serviceOfferingActions', sortable: false, width: '25%' },
        { text: 'Source', value: 'scmBased', sortable: true, width: '5%' },
        { text: '', value: 'data-table-expand', width: '5%' },
      ]
    },
    ServiceOfferingVersionsTableHeaders () {
      return [
        { text: 'Version', value: 'version', sortable: true, width: '15%' },
        { text: 'Id', value: 'id', sortable: true, width: '35%' },
        { text: 'Created', value: 'created', sortable: true, width: '20%' },
        { text: 'Actions', value: 'serviceOfferingActions', sortable: false, width: '30%' },
      ]
    },
  },
  methods: {
    emitInterface() {
      this.$emit("interface", {
        updateExpanded: () => {
          if (this.expandedServiceOffering != undefined) {
            console.log("Updated expanded")
            ServiceOfferingVersionsRestApi.getVersionsOfServiceOffering(this.expandedServiceOffering.id).then(response => {
              this.serviceOfferingVersionOfServiceOfferingId[this.expandedServiceOffering.id] = response;
              this.expandedServiceOfferingVersions = response
            })
          }
        }
      });
    },
    onEditServiceOfferingClicked (serviceOffering) {
      this.$router.push({ path: `/services/vendors/${serviceOffering.serviceVendorId}/offerings/${serviceOffering.id}` })
    },
    onDeleteServiceOfferingClicked (serviceOffering) {
      this.$emit('service-offering-delete', serviceOffering)
    },
    onAddServiceOfferingVersionClicked(serviceOffering) {
      this.$router.push({ path: `/services/vendors/${serviceOffering.serviceVendorId}` +
            `/offerings/${serviceOffering.id}/versions/` })
    },
    onEditServiceOfferingVersionClicked (serviceOfferingVersion) {
      this.$router.push({ path: `/services/vendors/${this.expandedServiceOffering.serviceVendorId}` +
                                `/offerings/${serviceOfferingVersion.serviceOfferingId}` +
                                `/versions/${serviceOfferingVersion.id}` })
    },
    onDeleteServiceOfferingVersionClicked (serviceOfferingVersion) {
      this.$emit('service-offering-version-delete', serviceOfferingVersion)
    },
    expandRow (serviceOffering) {
      const index = this.expanded.indexOf(serviceOffering)
      if (index === -1) {
        this.expanded = []
        this.expanded.push(serviceOffering)
        this.expandedServiceOffering = serviceOffering
        if (this.serviceOfferingVersionOfServiceOfferingId[serviceOffering.id] == undefined) {
          this.expandedServiceOfferingVersions = undefined
          ServiceOfferingVersionsRestApi.getVersionsOfServiceOffering(serviceOffering.id).then(response => {
            this.serviceOfferingVersionOfServiceOfferingId[serviceOffering.id] = response;
            this.expandedServiceOfferingVersions = response
          })}
          else {
            this.expandedServiceOfferingVersions = this.serviceOfferingVersionOfServiceOfferingId[serviceOffering.id]
          }

      } else {
        this.expanded.splice(index, 1)
        this.expandedServiceOffering = undefined
      }
    },
  }
}
</script>

<style scoped>

</style>
