<template>
  <div 
    class="banner-character" 
    :class="characterClass"
  >
    <div class="eyes-container">
      <div class="eye-ball" ref="leftEye">
        <div class="pupil" ref="leftPupil"></div>
      </div>
      <div class="eye-ball" ref="rightEye">
        <div class="pupil" ref="rightPupil"></div>
      </div>
    </div>
    <div class="mouth" :class="{ 'mouth-open': isOpen }"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'purple'
  },
  openMouth: {
    type: Boolean,
    default: false
  }
})

const leftEye = ref(null)
const rightEye = ref(null)
const leftPupil = ref(null)
const rightPupil = ref(null)
const isOpen = ref(false)
let blinkTimeout = null
let mouseMoveHandler = null

const characterClass = computed(() => {
  return `banner-${props.type}-character`
})

watch(() => props.openMouth, (newVal) => {
  isOpen.value = newVal
})

const blink = () => {
  const eyes = [leftEye.value, rightEye.value].filter(Boolean)
  eyes.forEach(eye => {
    eye.style.height = '4px'
    setTimeout(() => {
      eye.style.height = ''
    }, 150)
  })
  
  blinkTimeout = setTimeout(blink, Math.random() * 7000 + 6000)
}

const updatePupils = (e) => {
  if (!leftPupil.value || !rightPupil.value) return
  
  const calculatePosition = (element, maxDistance = 12) => {
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
.banner-character {
  position: relative;
  width: 200px;
  height: 250px;
  border-radius: 20px 20px 0 0;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform-origin: bottom center;
  flex-shrink: 0;
}

.banner-purple-character {
  background-color: #6C3FF5;
  height: 240px;
}

.banner-black-character {
  background-color: #2D2D2D;
  height: 200px;
}

.banner-orange-character {
  background-color: #FF9B6B;
  border-radius: 100px 100px 0 0;
  height: 160px;
}

.banner-yellow-character {
  background-color: #E8D754;
  border-radius: 70px 70px 0 0;
  height: 180px;
}

.banner-character .eyes-container {
  position: absolute;
  display: flex;
  gap: 32px;
  left: 50%;
  top: 50px;
  transform: translateX(-50%);
}

.banner-black-character .eyes-container {
  top: 35px;
  gap: 24px;
}

.banner-orange-character .eyes-container {
  top: 45px;
  gap: 36px;
}

.banner-yellow-character .eyes-container {
  top: 40px;
  gap: 28px;
}

.banner-character .eye-ball {
  width: 48px;
  height: 48px;
  background-color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.15s ease-out;
}

.banner-black-character .eye-ball {
  width: 36px;
  height: 36px;
}

.banner-orange-character .eye-ball {
  width: 44px;
  height: 44px;
}

.banner-yellow-character .eye-ball {
  width: 40px;
  height: 40px;
}

.banner-character .pupil {
  width: 18px;
  height: 18px;
  background-color: #2D2D2D;
  border-radius: 50%;
  transition: transform 0.1s ease-out;
}

.banner-black-character .pupil {
  width: 14px;
  height: 14px;
}

.banner-orange-character .pupil {
  width: 16px;
  height: 16px;
}

.banner-yellow-character .pupil {
  width: 15px;
  height: 15px;
}

.banner-character .mouth {
  position: absolute;
  width: 40px;
  height: 6px;
  background-color: #2D2D2D;
  border-radius: 3px;
  left: 50%;
  top: 140px;
  transform: translateX(-50%);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.banner-black-character .mouth {
  top: 110px;
  width: 32px;
  height: 5px;
}

.banner-orange-character .mouth {
  top: 115px;
  width: 44px;
  height: 5px;
}

.banner-yellow-character .mouth {
  top: 105px;
  width: 36px;
  height: 5px;
}

.banner-character .mouth-open {
  width: 60px;
  height: 36px;
  border-radius: 0 0 30px 30px;
}

.banner-black-character .mouth-open {
  width: 48px;
  height: 28px;
  border-radius: 0 0 24px 24px;
}

.banner-orange-character .mouth-open {
  width: 64px;
  height: 32px;
  border-radius: 0 0 28px 28px;
}

.banner-yellow-character .mouth-open {
  width: 52px;
  height: 30px;
  border-radius: 0 0 26px 26px;
}
</style>
