<template>
  <v-container
    fluid
    tag="section"
  >
    <base-material-card color="secondary">
      <template #heading>
        <overview-heading text="User Management" />
      </template>

      <div class="d-flex justify-end mb-4">
        <v-btn
          color="secondary"
          :disabled="!hasSelectedUsers || deleteUsersInProgress"
          @click="onDeleteSelectedClicked"
        >
          <v-icon>
            mdi-delete
          </v-icon>
        </v-btn>
      </div>

      <v-data-table
        v-model="selectedUsernames"
        :headers="userTableHeaders"
        :items="users"
        item-key="username"
        item-value="username"
        show-select
      >
        <template #item.admin="{ item }">
          <v-chip
            size="small"
            :color="item.admin ? 'success' : 'default'"
          >
            {{ item.admin ? 'Admin' : 'User' }}
          </v-chip>
        </template>
      </v-data-table>
    </base-material-card>

    <CustomDialog
      :show="showCreateUserDialog"
      title="Create User"
      :width="'700'"
      @canceled="onCancelCreateUser"
    >
      <template #content>
        <ValidationForm ref="createUserValidationForm">
          <v-row>
            <v-col cols="12" md="6">
              <Field
                v-slot="{ field, errors }"
                v-model="createUserForm.username"
                name="Username"
                :rules="usernameRule"
              >
                <v-text-field
                  v-bind="field"
                  label="Username"
                  :error-messages="errors"
                  :model-value="createUserForm.username"
                />
              </Field>
            </v-col>
            <v-col cols="12" md="6">
              <Field
                v-slot="{ field, errors }"
                v-model="createUserForm.email"
                name="Email"
                :rules="emailRule"
              >
                <v-text-field
                  v-bind="field"
                  label="Email"
                  :error-messages="errors"
                  :model-value="createUserForm.email"
                />
              </Field>
            </v-col>
            <v-col cols="12" md="6">
              <Field
                v-slot="{ field, errors }"
                v-model="createUserForm.firstName"
                name="First Name"
                :rules="firstNameRule"
              >
                <v-text-field
                  v-bind="field"
                  label="First Name"
                  :error-messages="errors"
                  :model-value="createUserForm.firstName"
                />
              </Field>
            </v-col>
            <v-col cols="12" md="6">
              <Field
                v-slot="{ field, errors }"
                v-model="createUserForm.lastName"
                name="Last Name"
                :rules="lastNameRule"
              >
                <v-text-field
                  v-bind="field"
                  label="Last Name"
                  :error-messages="errors"
                  :model-value="createUserForm.lastName"
                />
              </Field>
            </v-col>
            <v-col cols="12">
              <Field
                v-slot="{ field, errors }"
                v-model="createUserForm.password"
                name="Password"
                :rules="passwordRule"
              >
                <v-text-field
                  v-bind="field"
                  label="Password"
                  type="password"
                  :error-messages="errors"
                  :model-value="createUserForm.password"
                />
              </Field>
            </v-col>
            <v-col cols="12">
              <v-checkbox
                v-model="createUserForm.isAdmin"
                label="Create as admin"
              />
              <v-checkbox
                v-model="createUserForm.isPasswordTemporary"
                label="Temporary password"
              />
            </v-col>
          </v-row>
        </ValidationForm>
      </template>

      <template #actions>
        <v-spacer />
        <v-btn @click="onCancelCreateUser">Cancel</v-btn>
        <v-btn
          color="primary"
          :loading="createUserInProgress"
          :disabled="createUserInProgress"
          @click="onCreateUser"
        >
          Create
        </v-btn>
      </template>
    </CustomDialog>

    <ConfirmDialog
      :show="showDeleteUsersDialog"
      title="Delete users"
      :text="deleteConfirmationText"
      confirm-button-label="Delete"
      cancel-button-label="Cancel"
      :confirm-loading="deleteUsersInProgress"
      :attention="true"
      @canceled="onCancelDeleteUsers"
      @confirmed="onDeleteUsersConfirmed"
    />

    <v-btn
      color="secondary"
      icon="mdi-plus"
      class="create-user-fab"
      @click="showCreateUserDialog = true"
    />
  </v-container>
</template>

<script>
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import CustomDialog from "@/components/base/CustomDialog.vue";
import PlatformManagementClient from "@/api/platform-management/platform-management-client";
import { Field, Form as ValidationForm } from "vee-validate";
import * as yup from "yup";

const createEmptyUserForm = () => ({
  username: '',
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  isPasswordTemporary: false,
  isAdmin: false,
});

export default {
  name: 'AdminUsersPage',
  components: {
    OverviewHeading,
    ConfirmDialog,
    CustomDialog,
    Field,
    ValidationForm,
  },
  setup() {
    const usernameRule = yup
      .string()
      .required('Username is required')
      .matches(/^[A-Za-z][A-Za-z0-9_-]*$/, "Username must start with a letter and contain only letters, numbers, '-' and '_'");

    const firstNameRule = yup
      .string()
      .required('This field is required')
      .matches(/^[A-Za-z][A-Za-z .-]*$/, "Must start with a letter and may only contain letters, spaces, '.' and '-'");

    const lastNameRule = yup
      .string()
      .required('This field is required')
      .matches(/^[A-Za-z][A-Za-z.-]*$/, "Must start with a letter and may only contain letters, '.' and '-'");

    const emailRule = yup
      .string()
      .required('Email is required')
      .email('Email must be a valid email address');

    const passwordRule = yup
      .string()
      .required('Password is required');

    return {
      usernameRule,
      firstNameRule: firstNameRule.label('First name'),
      lastNameRule: lastNameRule.label('Last name'),
      emailRule,
      passwordRule,
    };
  },
  data() {
    return {
      users: [],
      showCreateUserDialog: false,
      showDeleteUsersDialog: false,
      selectedUsernames: [],
      createUserForm: createEmptyUserForm(),
      createUserInProgress: false,
      deleteUsersInProgress: false,
    };
  },
  computed: {
    userTableHeaders() {
      return [
        { title: 'Username', key: 'username', sortable: true },
        { title: 'First Name', key: 'firstName', sortable: true },
        { title: 'Last Name', key: 'lastName', sortable: true },
        { title: 'Email', key: 'email', sortable: true },
        { title: 'Role', key: 'admin', sortable: true },
      ];
    },
    hasSelectedUsers() {
      return this.selectedUsernames.length > 0;
    },
    deleteConfirmationText() {
      const selectedCount = this.selectedUsernames.length;
      const pluralSuffix = selectedCount === 1 ? '' : 's';
      return `Are you sure you want to delete ${selectedCount} selected user${pluralSuffix}?`;
    },
  },
  created() {
    this.loadUsers();
  },
  methods: {
    async loadUsers() {
      try {
        this.users = await PlatformManagementClient.getUsers();
      } catch (error) {
        this.$toast.error('Failed to load users');
      }
    },
    onDeleteSelectedClicked() {
      if (!this.hasSelectedUsers) {
        return;
      }

      this.showDeleteUsersDialog = true;
    },
    onCancelCreateUser() {
      this.showCreateUserDialog = false;
      this.createUserForm = createEmptyUserForm();
      this.$nextTick(() => {
        this.$refs.createUserValidationForm?.resetForm();
      });
    },
    onCancelDeleteUsers() {
      this.showDeleteUsersDialog = false;
    },
    async onDeleteUsersConfirmed() {
      if (!this.hasSelectedUsers) {
        return;
      }

      this.deleteUsersInProgress = true;
      const usernamesToDelete = [...this.selectedUsernames];
      let deletedUsersCount = 0;

      try {
        for (const username of usernamesToDelete) {
          try {
            await PlatformManagementClient.deleteUser(username);
            deletedUsersCount += 1;
          } catch (error) {
            const errorMessage = error?.response?.data?.message;
            this.$toast.error(errorMessage || `Failed to delete user '${username}'`);
          }
        }

        if (deletedUsersCount > 0) {
          const pluralSuffix = deletedUsersCount === 1 ? '' : 's';
          this.$toast.info(`Deleted ${deletedUsersCount} user${pluralSuffix}`);
        }

        this.onCancelDeleteUsers();
        this.selectedUsernames = [];
        await this.loadUsers();
      } finally {
        this.deleteUsersInProgress = false;
      }
    },
    async onCreateUser() {
      const validationResult = await this.$refs.createUserValidationForm?.validate();

      if (!validationResult?.valid) {
        this.$toast.error('Please fix the highlighted validation errors');
        return;
      }

      const sanitizedUserForm = {
        ...this.createUserForm,
        username: this.createUserForm.username.trim(),
        firstName: this.createUserForm.firstName.trim(),
        lastName: this.createUserForm.lastName.trim(),
        email: this.createUserForm.email.trim(),
      };

      this.createUserInProgress = true;

      try {
        await PlatformManagementClient.createUser(sanitizedUserForm);
        this.$toast.info(`User '${sanitizedUserForm.username}' created`);
        this.onCancelCreateUser();
        await this.loadUsers();
      } catch (error) {
        this.$toast.error('Failed to create user');
      } finally {
        this.createUserInProgress = false;
      }
    },
  },
};
</script>

<style scoped>
.create-user-fab {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 10;
}
</style>

