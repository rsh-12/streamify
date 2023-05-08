<script lang="ts">
	import { Table, tableMapperValues, type TableSource } from '@skeletonlabs/skeleton'
	import Heading from '../../components/atoms/heading.svelte'

	interface PlaylistTable {
		position: number
		title: string
		artist: string
		time: number
		album: string
	}

	const tableData: PlaylistTable[] = [
		{
			position: 1,
			title: 'Wonder',
			artist: 'Kanye West',
			time: 0,
			album: 'Graduation',
		},
		{
			position: 2,
			title: 'Champion',
			artist: 'Kanye West',
			time: 0,
			album: 'Graduation',
		},
	]

	const tableSimple: TableSource = {
		// A list of heading labels.
		head: ['#', 'Title', 'Artist', 'Time', 'Album'],
		// The data visibly shown in your table body UI.
		body: tableMapperValues(tableData, ['position', 'title', 'artist', 'time', 'album']),
		// Optional: The data returned when interactive is enabled and a row is clicked.
		meta: tableMapperValues(tableData, ['position', 'title', 'artist', 'time', 'album']),
	}

	//@ts-ignore
	function onSelected(meta: any): void {
		alert(meta.detail)
	}
</script>

<div class="">
	<section>
		<Heading url="/" link="More" title="Trending" />
		<div
			class="w-full flex justify-between mt-4 bg-white py-12 px-8 min-h-[300px] items-center rounded-3xl"
		>
			<div>
				<div>
					<p class="text-zinc-500">Artist</p>
					<h4 class="text-zinc-900 text-5xl">On Top Of The World</h4>
				</div>
				<div class="mt-8">
					<button class="btn variant-filled-surface">Play</button>
					<button class="btn variant-ringed-surface text-surface-900">Follow</button>
				</div>
			</div>
			<div class="text-zinc-800 text-end">
				<p>Montly Listener</p>
				<p class="font-bold">32,092</p>
			</div>
		</div>
	</section>
	<section class="mt-8">
		<Heading url="/" link="Show All" title="Playlist" />
		<div class="mt-4">
			<Table
				text="text-xl font-bold"
				source={tableSimple}
				interactive={true}
				on:selected={onSelected}
			/>
		</div>
	</section>
</div>
