package com.streamvault.data.local

import androidx.room.migration.Migration

/**
 * The single migration topology for the production database.
 *
 * Bodies remain close to the schema compatibility code in [StreamVaultDatabase], while this
 * registry is version-grouped and is the only list consumed by the database builder. The
 * contiguous-chain check fails at startup/build time if a migration is forgotten.
 */
object StreamVaultDatabaseMigrationRegistry {
    const val CURRENT_VERSION = STREAM_VAULT_DATABASE_VERSION

    val v1To24: List<Migration> = listOf(
        StreamVaultDatabase.MIGRATION_1_2,
        StreamVaultDatabase.MIGRATION_2_3,
        StreamVaultDatabase.MIGRATION_3_4,
        StreamVaultDatabase.MIGRATION_4_5,
        StreamVaultDatabase.MIGRATION_5_6,
        StreamVaultDatabase.MIGRATION_6_7,
        StreamVaultDatabase.MIGRATION_7_8,
        StreamVaultDatabase.MIGRATION_8_9,
        StreamVaultDatabase.MIGRATION_9_10,
        StreamVaultDatabase.MIGRATION_10_11,
        StreamVaultDatabase.MIGRATION_11_12,
        StreamVaultDatabase.MIGRATION_12_13,
        StreamVaultDatabase.MIGRATION_13_14,
        StreamVaultDatabase.MIGRATION_14_15,
        StreamVaultDatabase.MIGRATION_15_16,
        StreamVaultDatabase.MIGRATION_16_17,
        StreamVaultDatabase.MIGRATION_17_18,
        StreamVaultDatabase.MIGRATION_18_19,
        StreamVaultDatabase.MIGRATION_19_20,
        StreamVaultDatabase.MIGRATION_20_21,
        StreamVaultDatabase.MIGRATION_21_22,
        StreamVaultDatabase.MIGRATION_22_23,
        StreamVaultDatabase.MIGRATION_23_24
    )

    val v24To49: List<Migration> = listOf(
        StreamVaultDatabase.MIGRATION_24_25,
        StreamVaultDatabase.MIGRATION_25_26,
        StreamVaultDatabase.MIGRATION_26_27,
        StreamVaultDatabase.MIGRATION_27_28,
        StreamVaultDatabase.MIGRATION_28_29,
        StreamVaultDatabase.MIGRATION_29_30,
        StreamVaultDatabase.MIGRATION_30_31,
        StreamVaultDatabase.MIGRATION_31_32,
        StreamVaultDatabase.MIGRATION_32_33,
        StreamVaultDatabase.MIGRATION_33_34,
        StreamVaultDatabase.MIGRATION_34_35,
        StreamVaultDatabase.MIGRATION_35_36,
        StreamVaultDatabase.MIGRATION_36_37,
        StreamVaultDatabase.MIGRATION_37_38,
        StreamVaultDatabase.MIGRATION_38_39,
        StreamVaultDatabase.MIGRATION_39_40,
        StreamVaultDatabase.MIGRATION_40_41,
        StreamVaultDatabase.MIGRATION_41_42,
        StreamVaultDatabase.MIGRATION_42_43,
        StreamVaultDatabase.MIGRATION_43_44,
        StreamVaultDatabase.MIGRATION_44_45,
        StreamVaultDatabase.MIGRATION_45_46,
        StreamVaultDatabase.MIGRATION_46_47,
        StreamVaultDatabase.MIGRATION_47_48,
        StreamVaultDatabase.MIGRATION_48_49
    )

    val v49To75: List<Migration> = listOf(
        StreamVaultDatabase.MIGRATION_49_50,
        StreamVaultDatabase.MIGRATION_50_51,
        StreamVaultDatabase.MIGRATION_51_52,
        StreamVaultDatabase.MIGRATION_52_53,
        StreamVaultDatabase.MIGRATION_53_54,
        StreamVaultDatabase.MIGRATION_54_55,
        StreamVaultDatabase.MIGRATION_55_56,
        StreamVaultDatabase.MIGRATION_56_57,
        StreamVaultDatabase.MIGRATION_57_58,
        StreamVaultDatabase.MIGRATION_58_59,
        StreamVaultDatabase.MIGRATION_59_60,
        StreamVaultDatabase.MIGRATION_60_61,
        StreamVaultDatabase.MIGRATION_61_62,
        StreamVaultDatabase.MIGRATION_62_63,
        StreamVaultDatabase.MIGRATION_63_64,
        StreamVaultDatabase.MIGRATION_64_65,
        StreamVaultDatabase.MIGRATION_65_66,
        StreamVaultDatabase.MIGRATION_66_67,
        StreamVaultDatabase.MIGRATION_67_68,
        StreamVaultDatabase.MIGRATION_68_69,
        StreamVaultDatabase.MIGRATION_69_70,
        StreamVaultDatabase.MIGRATION_70_71,
        StreamVaultDatabase.MIGRATION_71_72,
        StreamVaultDatabase.MIGRATION_72_73,
        StreamVaultDatabase.MIGRATION_73_74,
        StreamVaultDatabase.MIGRATION_74_75
    )

    val all: List<Migration> = (v1To24 + v24To49 + v49To75).also(::validate)

    private fun validate(migrations: List<Migration>) {
        require(migrations.map { it.startVersion }.distinct().size == migrations.size) {
            "Duplicate Room migration start version"
        }
        val expected = 1 until CURRENT_VERSION
        require(migrations.map { it.startVersion }.toSet() == expected.toSet()) {
            "Room migration registry is incomplete: expected versions $expected"
        }
        require(migrations.all { it.endVersion == it.startVersion + 1 }) {
            "Room migration registry must contain adjacent migrations only"
        }
    }
}
