import commonjs from '@rollup/plugin-commonjs';
import resolve from "@rollup/plugin-node-resolve";
import copy from "rollup-plugin-copy";
import del from "rollup-plugin-delete";
import babel from "@rollup/plugin-babel";

export default {
    input: 'src/main/js/index.js',
    output: {
        dir: 'dist/js',
        format: 'module'
    },
    plugins: [
        commonjs(),
        resolve(),
        babel({babelHelpers: 'bundled'}),
        del({
            targets: 'dist/*',
        }),
        copy({
                targets: [
                    {
                        src: 'node_modules/govuk-frontend/dist/govuk/govuk-frontend.min.css',
                        dest: 'dist/css',
                    },
                    {
                        src: 'node_modules/govuk-frontend/dist/govuk/assets/images/**/*',
                        dest: 'dist/images',
                    },
                    {
                        src: 'node_modules/govuk-frontend/dist/govuk/assets/fonts/**/*',
                        dest: 'dist/fonts',
                    },
                    {
                        src: 'node_modules/govuk-frontend/dist/govuk/assets/manifest.json',
                        dest: 'dist',
                    },
                ],
            }
        )
    ]
};
