package org.rsmod.api.net.rsprot.player

import dev.openrune.definition.type.widget.ComponentType
import dev.openrune.definition.type.widget.IfEvent
import org.rsmod.game.ui.UserInterfaceMap

internal object InterfaceEvents {
    fun isEnabled(
        ui: UserInterfaceMap,
        component: ComponentType,
        comsub: Int,
        event: IfEvent,
    ): Boolean {
        val verifyStaticEvents = comsub == -1
        return if (verifyStaticEvents) {
            // Plain (non-inventory) components arrive with comsub -1. The component's statically
            // baked events are checked first, but runtime events granted via `ifSetEvents` must
            // count too - otherwise `ifSetEvents` is a silent no-op for plain buttons, and any
            // interface whose packed events bitmask doesn't align with this enum's bit layout
            // (e.g. custom interfaces authored against client-side masks) can never receive
            // clicks. Runtime ranges are stored clamped to 0..MAX (see ComponentEventMap), so
            // slot 0 is the canonical probe for a slotless component.
            component.hasEvent(event) || ui.hasEvent(component, 0, event)
        } else {
            ui.hasEvent(component, comsub, event)
        }
    }
}
