<template>
  <div 
    class="mini-character" 
    :class="characterClass"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <div class="eyes-container">
      <div class="eye-ball" ref="leftEye">
        <div class="pupil" ref="leftPupil"></div>
      </div>
      <div class="eye-ball" ref="rightEye">
        <div class="pupil" ref="rightPupil"></div>
      </div>
    </div>
    <div v-if="showMouth" class="mouth" :class="{ 'mouth-open': isOpenMouth }"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'purple'
  },
  openMouth: {
    type: Boolean,
    default: false
  },
  disableMouse: {
    type: Boolean,
    default: false
  }
})

const leftEye = ref(null)
const rightEye = ref(null)
const leftPupil = ref(null)
const rightPupil = ref(null)
const isStanding = ref(false)
let blinkTimeout = null
let mouseMoveHandler = null

const isOpenMouth = computed(() => props.openMouth)

const characterClass = computed(() => {
  return [
    `mini-${props.type}-character`,
    { 'standing': isStanding.value }
  ]
})

const showMouth = computed(() => {
  return true
})

const blink = () => {
  const eyes = [leftEye.value, rightEye.value].filter(Boolean)
  eyes.forEach(eye => {
    eye.style.height = '1px'
    setTimeout(() => {
      eye.style.height = ''
    }, 120)
  })
  
  blinkTimeout = setTimeout(blink, Math.random() * 7000 + 6000)
}

const updatePupils = (e) => {
  if (!leftPupil.value || !rightPupil.value) return
  
  const calculatePosition = (element, maxDistance = 2) => {
    if (!element) return { x: 0, y: 0 }
    
    const rect = element.getBoundingClientRect()
    const centerX = rect.left + rect.width / 2
    const centerY = rect.top + rect.height / 2
    
    const deltaX = e.clientX - centerX
    const deltaY = e.clientY - centerY
    
    const distance = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), maxDistance)
    const angle = Math.atan2(deltaY, deltaX)
    
    return { 
      x: Math.cos(angle) * distance, 
      y: Math.sin(angle) * distance 
    }
  }
  
  const leftPos = calculatePosition(leftPupil.value)
  leftPupil.value.style.transform = `translate(${leftPos.x}px, ${leftPos.y}px)`
  
  const rightPos = calculatePosition(rightPupil.value)
  rightPupil.value.style.transform = `translate(${rightPos.x}px, ${rightPos.y}px)`
}

const handleMouseEnter = () => {
  if (props.disableMouse) return
  isStanding.value = true
}

const handleMouseLeave = () => {
  if (props.disableMouse) return
  isStanding.value = false
}

onMounted(() => {
  setTimeout(blink, 2000)
  
  mouseMoveHandler = (e) => updatePupils(e)
  document.addEventListener('mousemove', mouseMoveHandler)
})

onUnmounted(() => {
  if (blinkTimeout) clearTimeout(blinkTimeout)
  if (mouseMoveHandler) document.removeEventListener('mousemove', mouseMoveHandler)
})
</script>

<style scoped>
.mini-character {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform-origin: bottom center;
}

.mini-character.standing {
  height: 44px;
  transform: translateY(-4px);
}

.mini-purple-character {
  background-color: #6C3FF5;
}

.mini-black-character {
  background-color: #2D2D2D;
}

.mini-orange-character {
  background-color: #FF9B6B;
  border-radius: 50% 50% 8px 8px;
}

.mini-yellow-character {
  background-color: #E8D754;
  border-radius: 50% 50% 8px 8px;
}

.mini-character .eyes-container {
  position: absolute;
  display: flex;
  gap: 6px;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -60%);
}

.mini-orange-character .eyes-container,
.mini-yellow-character .eyes-container {
  transform: translate(-50%, -40%);
}

.mini-character .eye-ball {
  width: 10px;
  height: 10px;
  background-color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.12s ease-out;
}

.mini-character .pupil {
  width: 4px;
  height: 4px;
  background-color: #2D2D2D;
  border-radius: 50%;
  transition: transform 0.1s ease-out;
}

.mini-character .mouth {
  position: absolute;
  width: 16px;
  height: 2px;
  background-color: #2D2D2D;
  border-radius: 1px;
  left: 50%;
  top: 70%;
  transform: translateX(-50%);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.mini-character .mouth-open {
  width: 20px;
  height: 12px;
  border-radius: 0 0 10px 10px;
}

.mini-orange-character .mouth,
.mini-yellow-character .mouth {
  top: 65%;
}
</style>
