<script lang="ts">
	interface Shortcut {
		name: string
		params: string
	}

	const shortcuts: Shortcut[] = [
		{
			name: 'Chill Hits',
			params: '/',
		},
		{
			name: 'Hop',
			params: '/feed',
		},
		{
			name: 'Accoustic',
			params: '/artist',
		},
		{
			name: 'Indie Pop',
			params: '/albums',
		},
		{
			name: 'Piano Blues',
			params: '/favorite',
		},
		{
			name: 'Jazz',
			params: '/favorite',
		},
	]
</script>

<div
	class="h-screen text-black font-bold max-w-[420px] bg-gray-50 p-6 flex flex-col justify-between"
>
	<div>
		<div class="mt-4">
			<h3>Shortcuts</h3>
			<ul class="w-full mt-8 flex flex-wrap gap-4">
				{#each shortcuts as shortcut}
					<li class="flex">
						<a
							class="btn bg-white justify-start rounded-full hover:bg-black w-full hover:text-white"
							href={shortcut.params}
						>
							<p>{shortcut.name}</p>
						</a>
					</li>
				{/each}
			</ul>
		</div>

		<div class="mt-8">
			<h3>Fav Artist</h3>
			<ul class="w-full mt-6 flex gap-4">
				<li class="flex w-full justify-between pr-4">
					<div class="flex">
						<img
							class="rounded-full w-12 h-12 bg-contain mr-4"
							src="/images/kanye.jpeg"
							alt="artist cover"
						/>
						<div class="flex flex-col gap-1">
							<p class="text-[12px]">Kanye West</p>
							<p class="text-zinc-500 text-[10px]">124 songs in library</p>
						</div>
					</div>
					<i class="rotate-90 text-lg">
						<svg xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 20 20"
							><path
								fill="currentColor"
								d="M10 9a2 2 0 1 1 0-4a2 2 0 0 1 0 4zm0 6a2 2 0 1 1 0-4a2 2 0 0 1 0 4z"
							/></svg
						>
					</i>
				</li>
			</ul>
		</div>
	</div>

	<div class="mt-8 mb-4">
		<div class="rounded-2xl bg-white w-full px-4 py-6">
			<img
				src="/images/playlist/gradutation.jpg"
				class="w-full rounded-xl max-h-[400px]"
				alt="playlist cover"
			/>
			<div class="flex justify-between mt-5">
				<div>
					<p class="text-zinc-800">Graduation</p>
					<p class="text-zinc-500 text-[11px]">Kanye West</p>
				</div>
				<button
					class="btn-icon-sm w-8 h-8 flex items-center justify-center rounded-md border border-zinc-900 text-zinc-900 text-xl"
				>
					<svg xmlns="http://www.w3.org/2000/svg" width="1em" height="1em" viewBox="0 0 24 24"
						><path fill="currentColor" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" /></svg
					>
				</button>
			</div>
		</div>
	</div>
</div>
