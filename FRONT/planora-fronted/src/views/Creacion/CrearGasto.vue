// CrearGastoPage.vue
<template>
  <ion-page>
    <ion-header :translucent="true" class="ion-no-border">
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button :default-href="`/dashboard/${grupoId}/gastos`" color="light" />
        </ion-buttons>
        <ion-title>
          <div class="title-container">
            <h1>Nuevo Gasto</h1>
            <p v-if="grupo?.nombre" class="subtitle">{{ grupo.nombre }}</p>
          </div>
        </ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <div v-if="cargando" class="loading-container">
        <ion-spinner name="crescent" color="primary" />
        <p>Cargando datos...</p>
      </div>
      <div v-else class="form-container">
        <FormularioBasico :formulario="formulario" :errores="errores" @validar="validarCampo" />
        <SeleccionParticipantes
          :usuarios="usuarios"
          :formulario="formulario"
          :errores="errores"
          @toggle="toggleParticipante"
          @validar="validarCampo"
        />
        <DivisionGasto
          :formulario="formulario"
          :participantes="participantesSeleccionados"
          :errores="errores"
          @cambio="onDivisionChange"
          @calcular="calcularTotalPersonalizado"
        />
        <SelectorEvento v-if="eventos.length" :formulario="formulario" :eventos="eventos" />
        <ResumenGasto :formulario="formulario" :participantes="participantesSeleccionados" :pagador="pagadorSeleccionado" />
        <BotonesAccion
          :valido="formularioValido"
          :enviando="enviando"
          @crear="crearGasto"
          @cancelar="cancelar"
        />
        <ion-toast :is-open="mostrarToast" :message="mensajeToast" :duration="3000" :color="tipoToast" @did-dismiss="mostrarToast = false" />
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useIonRouter, IonPage, IonHeader, IonToolbar, IonButtons, IonBackButton, IonTitle, IonContent, IonSpinner, IonToast } from '@ionic/vue';
import FormularioBasico from '@/views/Components/CreacionGasto/FormularioBasico.vue';
import SeleccionParticipantes from '@/views/Components/CreacionGasto/SeleccionParticipantes.vue';
import DivisionGasto from '@/views/Components/CreacionGasto/DivisionGasto.vue';
import SelectorEvento from '@/views/Components/CreacionGasto/SelectorEvento.vue';
import ResumenGasto from '@/views/Components/CreacionGasto/ResumenGasto.vue';
import BotonesAccion from '@/views/Components/CreacionGasto/BotonesAcciones.vue';
import GastoService from '@/service/GastosService';

// Router y ruta
const router = useIonRouter();
const route = useRoute();
const grupoId = Number(route.params.id);

// Estados
const cargando = ref(true);
const enviando = ref(false);
const usuarios = ref([]);
const eventos = ref([]);
const grupo = ref(null);

// Toast
const mostrarToast = ref(false);
const mensajeToast = ref('');
const tipoToast = ref('success');

// Formulario y errores
const formulario = reactive({
  titulo: '', monto: '', pagadoPorId: null,
  participantesIds: [], partesIguales: true,
  eventoId: null, cantidadesPersonalizadas: {}
});
const errores = reactive({ titulo: '', monto: '', pagadoPorId: '', participantes: '', cantidades: '' });

// Computed
const participantesSeleccionados = computed(() => usuarios.value.filter(u => formulario.participantesIds.includes(u.id)));
const pagadorSeleccionado = computed(() => usuarios.value.find(u => u.id === formulario.pagadoPorId));
const formularioValido = computed(() => {
  return formulario.titulo.trim() && formulario.monto && formulario.pagadoPorId && formulario.participantesIds.length > 0 &&
         (formulario.partesIguales || calcularTotalPersonalizado() === parseFloat(formulario.monto));
});

// Funciones
const mostrarMensaje = (msg, tipo='success') => { mensajeToast.value=msg; tipoToast.value=tipo; mostrarToast.value=true; };
const validarCampo = campo => { errores[campo] = ''; switch(campo){ case 'titulo': if(!formulario.titulo.trim()) errores.titulo='Obligatorio'; break; case 'monto': if(!(parseFloat(formulario.monto)>0)) errores.monto='Debe ser >0'; break; case 'pagadoPorId': if(!formulario.pagadoPorId) errores.pagadoPorId='Selecciona pagador'; break; } };
const toggleParticipante = id => {
  const idx = formulario.participantesIds.indexOf(id);
  idx>-1 ? formulario.participantesIds.splice(idx,1) : formulario.participantesIds.push(id);
};
const onDivisionChange = () => {
  if(formulario.partesIguales) formulario.cantidadesPersonalizadas={};
  else participantesSeleccionados.value.forEach(u => formulario.cantidadesPersonalizadas[u.id]='');
};
const calcularTotalPersonalizado = () => Object.values(formulario.cantidadesPersonalizadas).reduce((s,v)=>s+(parseFloat(v)||0),0);
const crearGasto = async () => {
  validarCampo('titulo'); validarCampo('monto'); validarCampo('pagadoPorId');
  if(formulario.participantesIds.length===0){ errores.participantes='Al menos 1'; return; }
  if(!formulario.partesIguales && Math.abs(calcularTotalPersonalizado()-parseFloat(formulario.monto))>0.01){ errores.cantidades='Suma != total'; return; }
  enviando.value=true;
  try{
    const payload = { titulo:formulario.titulo.trim(), monto:parseFloat(formulario.monto), pagadoPorId:formulario.pagadoPorId, grupoId, eventoId:formulario.eventoId, partesIguales:formulario.partesIguales, participantesIds:formulario.participantesIds, cantidadesPersonalizadas: formulario.partesIguales?null:formulario.cantidadesPersonalizadas };
    await GastoService.crearGasto(grupoId,payload);
    mostrarMensaje('Gasto creado');
    setTimeout(()=>router.push(`/dashboard/${grupoId}`),1500);
  }catch(e){ mostrarMensaje('Error al crear','danger'); }
  finally{ enviando.value=false; }
};
const cancelar = () => router.push(`/dashboard/${grupoId}`);

// Carga datos
const cargarDatos = async () => {
  cargando.value = true;
  try {
    // Ahora existe obtenerGrupo()
    [ grupo.value, usuarios.value, eventos.value ] = await Promise.all([
      GastoService.obtenerGrupo(grupoId),
      GastoService.obtenerUsuarios(grupoId),
      GastoService.obtenerEventos(grupoId)
    ]);
    // Preseleccionar al usuario actual si está en localStorage
    const usr = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (usr.id) {
      formulario.pagadoPorId = usr.id;
      formulario.participantesIds = [usr.id];
    }
  } catch (e) {
    // Log completo en consola
    console.error('Error cargando datos de creación de gasto:', e);
    // Mostrar mensaje concreto
    mostrarMensaje(`Error cargando datos: ${e.message || 'Revisa consola'}`, 'danger');
  } finally {
    cargando.value = false;
  }
 };
onMounted(cargarDatos);
</script>

<style scoped>
.title-container h1{margin:0;font-size:20px;font-weight:700}
.subtitle{margin:0;font-size:14px;color:var(--ion-color-medium)}
.loading-container{display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:200px;gap:16px}
.form-container{max-width:600px;margin:0 auto}
</style>

// (Los otros componentes permanecen igual)
