<template>
  <ion-card class="calendar-card">
    <ion-card-header>
      <div class="header">
        <ion-button fill="clear" size="small" @click="$emit('prev-month')">
          <ion-icon icon="chevron-back" />
        </ion-button>
        <h3 class="title">{{ currentMonthYear }}</h3>
        <ion-button fill="clear" size="small" @click="$emit('next-month')">
          <ion-icon icon="chevron-forward" />
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
          :class="[
            'day',
            { 'other-month': !day.isCurrentMonth },
            { today: day.isToday },
            { selected: day.isSelected },
            { 'has-event': eventDates.includes(day.date.toDateString()) }
          ]"
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
import type { CalendarDay } from '@/Composable/useCalendar'

const props = defineProps<{
  headerDays: string[]
  currentMonthYear: string
  calendarDays: CalendarDay[]
  eventDates: string[]
}>()
</script>

<style scoped lang="scss">
.calendar-card {
  @include card-base;
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    .title {
      margin: 0;
      font-size: 1.25rem;
    }
  }
  .weekdays {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    text-align: center;
    font-weight: 500;
    margin-top: 0.5rem;
  }
  .days-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    margin-top: 0.5rem;
    .day {
      padding: 0.5rem;
      text-align: center;
      cursor: pointer;
      &.other-month { color: var(--ion-color-medium); }
      &.today { border: 1px solid var(--ion-color-primary); border-radius: 0.25rem; }
      &.selected { background-color: var(--ion-color-primary-tint); border-radius: 0.25rem; }
      &.has-event { background-color: var(--ion-color-secondary-tint); border-radius: 0.25rem; }
    }
  }
}
</style>