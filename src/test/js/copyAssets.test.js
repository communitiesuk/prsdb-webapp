import assert from 'node:assert/strict';
import { mkdtemp, mkdir, readFile, readdir, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { afterEach, beforeEach, describe, test } from 'node:test';
import copyAssets from '../../../scripts/copy-assets.mjs';

describe('Build asset copying', () => {
    let root;

    beforeEach(async () => {
        root = await mkdtemp(join(tmpdir(), 'prsdb-copy-assets-'));
    });

    afterEach(async () => {
        await rm(root, { recursive: true, force: true });
    });

    test('copies a file into a destination directory that does not exist yet', async () => {
        const src = join(root, 'manifest.json');
        const dest = join(root, 'dist', 'rebrand');
        await writeFile(src, '{"name":"assets"}');

        await copyAssets({ targets: [{ src, dest }] }).buildEnd();

        assert.equal(await readFile(join(dest, 'manifest.json'), 'utf8'), '{"name":"assets"}');
    });

    test('flattens directory contents and lets later targets overwrite earlier files', async () => {
        const src = join(root, 'images');
        const dest = join(root, 'dist', 'images');
        const replacement = join(root, 'logo.svg');
        await mkdir(join(src, 'nested'), { recursive: true });
        await writeFile(join(src, 'nested', 'icon.svg'), 'icon');
        await writeFile(join(src, 'logo.svg'), 'original');
        await writeFile(replacement, 'replacement');

        await copyAssets({ targets: [{ src, dest }, { src: replacement, dest }] }).buildEnd();

        assert.deepEqual((await readdir(dest)).sort(), ['icon.svg', 'logo.svg']);
        assert.equal(await readFile(join(dest, 'icon.svg'), 'utf8'), 'icon');
        assert.equal(await readFile(join(dest, 'logo.svg'), 'utf8'), 'replacement');
    });

    test('skips absent optional asset directories and continues copying other targets', async () => {
        const src = join(root, 'style.css');
        const dest = join(root, 'dist');
        await writeFile(src, 'body {}');

        await copyAssets({ targets: [
            { src: join(root, 'missing-fonts'), dest: join(dest, 'fonts') },
            { src, dest },
        ] }).buildEnd();

        assert.deepEqual(await readdir(dest), ['style.css']);
    });
});
