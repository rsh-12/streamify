<script lang="ts">
	import AppLogo from '../atoms/app-logo.svelte'

	interface Links {
		name: string
		url: string
		icon?: string
	}

	const links: Links[] = [
		{
			name: 'Home',
			url: '/',
		},
		{
			name: 'Feed',
			url: '/feed',
		},
		{
			name: 'Artist',
			url: '/artist',
		},
		{
			name: 'Albums',
			url: '/albums',
		},
		{
			name: 'Favorite Songs',
			url: '/favorite',
		},
	]
</script>

<div class="h-screen bg-white text-black font-bold min-w-[320px] flex flex-col px-6 py-8">
	<nav class="w-full">
		<div class="flex justify-between items-center">
			<AppLogo />
			<button class="btn btn-icon self-end">
				<svg xmlns="http://www.w3.org/2000/svg" width="1.5em" height="1.5em" viewBox="0 0 24 24">
					<path
						fill="currentColor"
						d="M16.5 14.8V9.2q0-.35-.3-.475t-.55.125L13.2 11.3q-.3.3-.3.7t.3.7l2.45 2.45q.25.25.55.125t.3-.475ZM5 21q-.825 0-1.413-.588T3 19V5q0-.825.588-1.413T5 3h14q.825 0 1.413.588T21 5v14q0 .825-.588 1.413T19 21H5Zm5-2h9V5h-9v14Z"
					/>
				</svg>
			</button>
		</div>
		<ul class="w-full mt-8 flex flex-col gap-1">
			{#each links as link}
				<li class="w-full flex">
					<a
						class="btn justify-start rounded-lg hover:bg-black w-full hover:text-white"
						href={link.url}
					>
						{#if link.icon}
							<span>{link.icon}</span>
						{:else}
							<i>
								<svg xmlns="http://www.w3.org/2000/svg" width="1em" height="1em" viewBox="0 0 24 24"
									><g fill="none"
										><path
											fill="currentColor"
											d="M16 3H8a2 2 0 0 0-2 2v16l6-3l6 3V5a2 2 0 0 0-2-2Z"
											opacity=".16"
										/><path
											stroke="currentColor"
											d="M16 3H8a2 2 0 0 0-2 2v16l6-3l6 3V5a2 2 0 0 0-2-2Z"
										/></g
									></svg
								>
							</i>
						{/if}

						<p>{link.name}</p>
					</a>
				</li>
			{:else}
				<li>Empty list</li>
			{/each}
		</ul>
	</nav>
</div>
