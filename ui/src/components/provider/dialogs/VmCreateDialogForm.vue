<template>
  <v-form>
    <v-row
      v-for="param in parameter"
      :key="param.variable"
    >
      <v-text-field
        v-if="param.type=='text'"
        :label="param.question_name"
      />
      <v-text-field
        v-if="param.type=='password'"
        type="password"
        :label="param.question_name"
      />
      <v-select
        v-if="param.type=='multiplechoice'"
        :label="param.question_name"
        :items="param.choices"
      />
      <v-text-field
        v-if="param.type=='integer'"
        single-line
        type="number"
        :min="param.min"
        :max="param.max"
      />
      <v-select
        v-if="param.type=='multiselect'"
        multiple
        chips
        :items="param.choices"
      />
      <v-tooltip top>
        <template #activator="{ on, attrs }">
          <v-icon
            v-bind="attrs"
            class="ml-8"
            v-on="on"
          >
            mdi-information
          </v-icon>
        </template>
        <span>{{ param.question_description }}</span>
      </v-tooltip>
    </v-row>
  </v-form>
</template>

<script>
export default {
  name: 'VmCreateDialogForm',
  props: ['parameter']
}
</script>