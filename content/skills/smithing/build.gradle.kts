plugins {
    id("base-conventions")

}

dependencies {
    implementation(projects.api.invStorage)
    implementation(projects.api.pluginCommons)
    implementation(projects.api.attr)
    implementation(projects.content.skills.utils)
    // The furnace's smelt op falls back to the gold crafting interface when there is no ore to
    // smelt but gold bars are held (see SmeltingScript).
    implementation(projects.content.skills.crafting)
}
