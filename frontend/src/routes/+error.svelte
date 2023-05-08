<script lang="ts">
	import { page } from '$app/stores'
</script>

<div class="flex justify-between items-center text-zinc-800">
	<div>
		<h2 class="font-bold">Error: {$page.error?.message}</h2>
		<p class="mt-4 text-zinc-600">Something went wrong</p>
		<pre class="mt-4">
      {JSON.stringify($page, null, 2)}
    </pre>
	</div>
</div>
