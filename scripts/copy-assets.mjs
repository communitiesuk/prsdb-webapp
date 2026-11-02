import { copyFile, mkdir, readdir, stat } from 'node:fs/promises';
import { basename, join } from 'node:path';

export default function copyAssets({ targets }) {
    return {
        name: 'copy-assets',
        async buildEnd() {
            for (const { src, dest } of targets) {
                const source = await stat(src).catch(error => {
                    if (error.code === 'ENOENT') return null;
                    throw error;
                });
                if (!source) continue;

                const files = source.isDirectory()
                    ? (await readdir(src, { recursive: true })).map(file => join(src, file))
                    : [src];

                for (const file of files) {
                    if (!(await stat(file)).isFile()) continue;
                    await mkdir(dest, { recursive: true });
                    await copyFile(file, join(dest, basename(file)));
                }
            }
        },
    };
}
