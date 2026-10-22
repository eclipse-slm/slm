import { defineConfig } from 'vite';
import { slmUi } from '@eclipse-slm/ui/vite';

export default defineConfig({
    plugins: [slmUi({ overridesDir: 'src/overrides', apiClientsDir: '../src/api' })],
});
