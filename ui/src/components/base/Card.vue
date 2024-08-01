<script>
import {VCard} from 'vuetify/components'

export default {
    // eslint-disable-next-line vue/multi-word-component-names
    name: 'Card',

    extends: VCard,
    setup(props, ctx) {
      console.log("TEst");
      return {
        ...VCard.setup(props, ctx),
        // local bindings
      }
    }
  }
</script>
