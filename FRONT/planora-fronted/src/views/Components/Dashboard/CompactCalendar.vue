<template>
  <ion-card class="calendar-card">
    <ion-card-header>
      <div class="header">
        <ion-button fill="clear" size="small" @click="$emit('prev-month')">
          <ion-icon :icon="chevronBack" />
        </ion-button>
        <h3 class="title">{{ currentMonthYear }}</h3>
        <ion-button fill="clear" size="small" @click="$emit('next-month')">
          <ion-icon :icon="chevronForward" />
        </ion-button>
      </div>
      <div class="weekdays">
        <div v-for="day in headerDays" :key="day" class="weekday">{{ day }}</div>
      </div>
    </ion-card-header>
    <ion-card-content>
      <div class="days-grid">
        <div
          v-for="day in calendarDays"
          :key="day.date.toISOString()"
          @click="$emit('select-day', day)"
  :class="{
            day: true,
            'other-month': !day.isCurrentMonth,
            today: day.isToday,
            selected: day.isSelected,
            'has-event': eventDates.includes(day.date.toDateString()),
            'event-past': pastEventDates.includes(day.date.toDateString())
          }"
        >
          {{ day.day }}
        </div>
      </div>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import { IonCard, IonCardHeader, IonCardContent, IonButton, IonIcon } from '@ionic/vue'
import { chevronBack, chevronForward } from 'ionicons/icons'
import type { CalendarDay } from '@/Composable/useCalendar'

const props = defineProps<{
  headerDays: string[]
  currentMonthYear: string
  calendarDays: CalendarDay[]
  eventDates: string[]
  pastEventDates: string[]
}>()
</script>

<style scoped lang="scss">
.calendar-card {
  background: var(--ion-color-surface);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow-sm);

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: var(--spacing-unit);
    .title {
      font-size: var(--font-size-md);
      font-weight: 500;
      margin: 0;
    }
  }

  .weekdays {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    text-align: center;
    font-size: var(--font-size-sm);
    font-weight: 600;
    color: var(--ion-color-medium);
    margin-bottom: var(--spacing-unit);
  }

  .days-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 4px;

    .day {
      padding: calc(var(--spacing-unit) * 0.75);
      text-align: center;
      border-radius: var(--border-radius);
      cursor: pointer;
      transition: background 0.2s, transform 0.1s;
      &:hover { transform: scale(1.05); }

      &.other-month { color: var(--ion-color-medium); }
      &.today {
        border: 2px solid var(--ion-color-primary);
      }
      &.selected {
        background: var(--ion-color-primary-tint);
      }
       &.event-past {
        background: var(--ion-color-medium);
        color: #fff;
      }
      &.has-event {
        background: var(--ion-color-secondary-tint);
      }
    }
  }
}
</style>
