import { mount } from '@vue/test-utils'
import StatsGrid from '../../planora-fronted/src/views/Components/Dashboard/StatsGrid.vue'
import { describe, it, expect } from 'vitest'

describe('StatsGrid.vue', () => {
  it('renders provided stats', () => {
    const wrapper = mount(StatsGrid, {
      props: { membersCount: 5, upcomingCount: 2 },
      global: {
        stubs: [
          'ion-grid',
          'ion-row',
          'ion-col',
          'ion-card',
          'ion-card-content',
          'ion-icon'
        ]
      }
    })

    const numbers = wrapper.findAll('.number')
    expect(numbers).toHaveLength(2)
    expect(numbers[0].text()).toBe('5')
    expect(numbers[1].text()).toBe('2')

    const labels = wrapper.findAll('.label')
    expect(labels.map(l => l.text())).toEqual(['Miembros', 'Próximos'])
  })
})