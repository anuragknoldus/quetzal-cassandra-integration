package com.knoldus.model

import java.util.UUID

case class EntityInfo(
                       isAvailable: Boolean,
                       location: Int,
                       isSpill: Boolean = false,
                       spill: Int = 0,
                       id: Option[UUID] = None
                     )
