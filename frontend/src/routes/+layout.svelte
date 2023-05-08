<script lang="ts">
	import '@skeletonlabs/skeleton/themes/theme-crimson.css'
	import '@skeletonlabs/skeleton/styles/all.css'
	import '../app.postcss'
	import { AppShell } from '@skeletonlabs/skeleton'
	import SidebarRight from '../components/ui/sidebar-right.svelte'
	import SidebarLeft from '../components/ui/sidebar-left.svelte'
	import Layout from '../components/ui/layout.svelte'
	import PageHeader from '../components/ui/page-header.svelte'
	import ModalEmbed from '../components/atoms/modal-embed.svelte'
</script>

<AppShell>
	<svelte:fragment slot="pageHeader">
		<PageHeader />
	</svelte:fragment>

	<svelte:fragment slot="sidebarLeft">
		<SidebarLeft />
	</svelte:fragment>

	<svelte:fragment slot="sidebarRight">
		<SidebarRight />
	</svelte:fragment>

	<!-- Page Route Content -->
	<Layout>
		<slot />
	</Layout>
	<!-- Page Route Content -->
</AppShell>

<!-- Modal Content -->
<ModalEmbed />
<!-- Modal Content -->
