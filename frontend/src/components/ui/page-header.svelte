<script lang="ts">
	import Layout from './layout.svelte'
	let inputPopupDemo: string = ''
</script>

<Layout>
	<section class=" flex gap-12 justify-center items-center">
		<div class="flex gap-8 text-zinc-900">
			<svg xmlns="http://www.w3.org/2000/svg" width="1.5em" height="1.5em" viewBox="0 0 512 512"
				><path
					fill="currentColor"
					d="M9.4 233.4c-12.5 12.5-12.5 32.8 0 45.3l128 128c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L109.3 288H480c17.7 0 32-14.3 32-32s-14.3-32-32-32H109.3l73.4-73.4c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0l-128 128z"
				/></svg
			>
			<svg xmlns="http://www.w3.org/2000/svg" width="1.5em" height="1.5em" viewBox="0 0 512 512"
				><path
					fill="currentColor"
					d="M502.6 278.6c12.5-12.5 12.5-32.8 0-45.3l-128-128c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3l73.4 73.4H32c-17.7 0-32 14.3-32 32s14.3 32 32 32h370.7l-73.4 73.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0l128-128z"
				/></svg
			>
		</div>
		<div class="flex justify-center rounded-full bg-white w-full">
			<div class="text-zinc-900 py-3 pl-5">
				<svg xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 24 24"
					><g id="feSearch0" fill="none" stroke="none"
						><g id="feSearch1" fill="black"
							><path
								id="feSearch2"
								d="m16.325 14.899l5.38 5.38a1.008 1.008 0 0 1-1.427 1.426l-5.38-5.38a8 8 0 1 1 1.426-1.426ZM10 16a6 6 0 1 0 0-12a6 6 0 0 0 0 12Z"
							/></g
						></g
					></svg
				>
			</div>
			<input
				placeholder="Search for artist, songs and ..."
				bind:value={inputPopupDemo}
				class=" p-3 bg-white w-full rounded-r-full"
			/>
		</div>
	</section>
</Layout>
