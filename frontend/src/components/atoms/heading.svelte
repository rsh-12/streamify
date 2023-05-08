<script lang="ts">
	export let title: string
	export let link: string
	export let url: string
</script>

<div class="flex justify-between items-center text-zinc-800">
	<h2 class="font-bold">{title}</h2>
	<a href={url} class="flex items-center gap-3 btn bg-initial hover:text-sky-500">
		<span class="">{link}</span>
		<svg xmlns="http://www.w3.org/2000/svg" width="1.5em" height="1.5em" viewBox="0 0 24 24"
			><path
				fill="currentColor"
				d="M8.025 22L6.25 20.225L14.475 12L6.25 3.775L8.025 2l10 10l-10 10Z"
			/></svg
		>
	</a>
</div>
