<template>
  <div class="min-h-screen flex flex-col bg-[#3A3331]">
    <Header />
    <main class="flex-grow pt-[60px] md:pt-[110px] pb-24 md:pb-0">
      <PersonalShell>
        <slot />
      </PersonalShell>
    </main>
    <Footer />
    <MobileBottomMenu />
  </div>
</template>
