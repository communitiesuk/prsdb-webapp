import commonjs from '@rollup/plugin-commonjs';
import resolve from "@rollup/plugin-node-resolve";
import copy from "rollup-plugin-copy";
import del from "rollup-plugin-delete";
import babel from "@rollup/plugin-babel";
import sass from 'rollup-plugin-sass';

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
        sass({
            output: 'dist/css/custom.css',
            options: {
                includePaths: ['node_modules/govuk-frontend/dist'],
            }
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
                    {
                        src: 'node_modules/govuk-one-login-service-header/dist/scripts/service-header.js',
                        dest: 'dist/js',
                    },
                    {
                        src: 'node_modules/govuk-one-login-service-header/dist/styles/service-header.css',
                        dest: 'dist/css',
                    },
                    {
                        src: 'node_modules/accessible-autocomplete/dist/accessible-autocomplete.min.css',
                        dest: 'dist/css'
                    },
                    {
                        src: 'node_modules/@ministryofjustice/frontend/moj/moj-frontend.min.css',
                        dest: 'dist/css'
                    },
                    {
                        src: 'node_modules/@ministryofjustice/frontend/moj/assets/images/**/*',
                        dest: 'dist/images',
                    },
                    {
                        src: 'node_modules/@ministryofjustice/frontend/moj/assets/fonts/**/*',
                        dest: 'dist/fonts',
                    },
                ],
            }
        )
    ]
};
