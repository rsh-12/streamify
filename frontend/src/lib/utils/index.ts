import { type ModalSettings, modalStore } from '@skeletonlabs/skeleton'

export function modalComponentEmbed(): void {
	const d: ModalSettings = {
		type: 'component',
		component: 'exampleEmbed',
	}
	modalStore.trigger(d)
}
